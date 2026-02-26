package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;

@FunctionalInterface
public interface PermanentSacrificeAction {
    void sacrifice(GameData gameData, Player player, Permanent permanent);
}
