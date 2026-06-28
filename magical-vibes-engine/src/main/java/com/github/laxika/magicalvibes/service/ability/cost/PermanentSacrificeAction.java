package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;

/**
 * Callback for sacrificing a permanent as part of an activated ability cost.
 *
 * <p>Decouples the cost handler implementations from the sacrifice logic in
 * {@link com.github.laxika.magicalvibes.service.ability.AbilityActivationService},
 * which provides the concrete implementation via method reference.
 */
@FunctionalInterface
public interface PermanentSacrificeAction {

    /**
     * Sacrifices the given permanent: removes it from the battlefield, moves the card to the
     * graveyard, and broadcasts a log message.
     */
    void sacrifice(GameData gameData, Player player, Permanent permanent);
}
