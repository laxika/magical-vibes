package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;

/**
 * Callback for exiling a permanent as part of an activated ability cost.
 */
@FunctionalInterface
public interface PermanentExileAction {

    /**
     * Removes the permanent from the battlefield and puts its card into its owner's exile zone.
     */
    void exile(GameData gameData, Player player, Permanent permanent);
}
