package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCommanderFromCommandZoneWithoutPayingManaCostEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Queues one free-cast offer for each commander currently in the command zone. */
@Component
@RequiredArgsConstructor
public class MayCastCommanderFromCommandZoneWithoutPayingManaCostEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastCommanderFromCommandZoneWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> commandZone = gameData.playerCommandZones.get(controllerId);
        if (commandZone == null) {
            return;
        }

        for (int i = commandZone.size() - 1; i >= 0; i--) {
            Card commander = commandZone.get(i);
            if (!gameData.isCommander(commander.getId())) {
                continue;
            }
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    commander,
                    controllerId,
                    List.of(effect),
                    "Cast " + commander.getName() + " without paying its mana cost?"
            ));
        }
    }
}
