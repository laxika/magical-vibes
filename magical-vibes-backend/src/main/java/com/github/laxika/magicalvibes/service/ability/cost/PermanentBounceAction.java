package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;

/**
 * Callback for returning a permanent to its owner's hand as part of an activated ability cost.
 *
 * <p>Decouples the cost handler implementations from the bounce logic in
 * {@link com.github.laxika.magicalvibes.service.ability.AbilityActivationService},
 * which provides the concrete implementation via method reference.
 */
@FunctionalInterface
public interface PermanentBounceAction {

    /**
     * Returns the given permanent to its owner's hand: removes it from the battlefield,
     * moves the card to the owner's hand, and broadcasts a log message.
     */
    void bounce(GameData gameData, Player player, Permanent permanent);
}
