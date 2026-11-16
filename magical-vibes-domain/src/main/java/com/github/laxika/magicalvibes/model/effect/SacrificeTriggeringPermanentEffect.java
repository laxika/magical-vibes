package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifices the permanent that fired this triggered ability ("sacrifice that permanent"), read
 * from {@code StackEntry.triggeringPermanentId}. Used by Equipment with sacrifice-on-unattach
 * (Grafted Wargear, Stitcher's Graft). A player can sacrifice only permanents they control, so
 * nothing happens when the triggering permanent is controlled by another player; if it has already
 * left the battlefield, nothing happens either. With {@code currentControllerSacrifices} its current controller
 * sacrifices it instead ("that creature's controller sacrifices it at end of combat", Basalt Golem), and
 * {@code tokenForSacrificingPlayer} is then created for that player only when the sacrifice succeeds ("if the player
 * does, they create ...").
 */
public record SacrificeTriggeringPermanentEffect(boolean currentControllerSacrifices,
                                                 CreateTokenEffect tokenForSacrificingPlayer)
        implements CardEffect {

    public SacrificeTriggeringPermanentEffect() {
        this(false, null);
    }
}
