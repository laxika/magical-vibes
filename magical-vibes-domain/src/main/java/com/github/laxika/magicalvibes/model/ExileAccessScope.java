package com.github.laxika.magicalvibes.model;

public enum ExileAccessScope {
    CONTROLLER,
    EXILER,
    ACTIVE_PLAYER,
    /**
     * The source's controller, but only while the source has never changed control since entering the
     * battlefield. Once control changes, access is lost for good, even if control later returns
     * (Taster of Wares: "for as long as you control this creature").
     */
    CONTROLLER_WITHOUT_CONTROL_CHANGE
}
