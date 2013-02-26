
var plugin = undefined;

function Plugin() {
    /** @private */
    this.container_ = document.getElementById("container");

    /** @private */
    this.screen_ = document.getElementById("screen");

    /** @private */
    this.button_ = document.getElementById("button");
    
    var rotation = 0;
    this.screen_.addEventListener("click", function() {
        this.changeRotation_((++rotation) % 4);
    }.bind(this));
}

/** @private */
Plugin.prototype.changeRotation_ = function(rotation) {
    const PADDING = 10;
    var deg = rotation * 90;
    this.screen_.style['-webkit-transform'] = 'rotate(-' + deg + 'deg)';
    
    var width, height, marginTop, marginLeft;
    if ((rotation & 1) == 1) {
        width = this.screen_.height + PADDING * 2;
        height = this.screen_.width + PADDING * 2;
        marginTop = PADDING - (width - height) / 2;
        marginLeft = PADDING - (height - width) / 2;
    } else {
        width = this.screen_.width + PADDING * 2;
        height = this.screen_.height + PADDING * 2;
        marginTop = PADDING;
        marginLeft = PADDING;
    }
    
    this.screen_.style['top'] = marginTop + 'px';
    this.screen_.style['left'] = marginLeft + 'px';
    
    this.button_.style['left'] = PADDING + 'px';
    this.button_.style['width'] = (width - PADDING * 2) + 'px';
    this.button_.style['top'] = (height - PADDING) + 'px';
    this.button_.style['bottom'] = PADDING + 'px';
};

window.onload=function() {
    if (plugin == undefined)
        plugin = new Plugin();
};
