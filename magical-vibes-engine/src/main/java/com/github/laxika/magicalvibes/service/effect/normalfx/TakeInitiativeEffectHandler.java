package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TakeInitiativeEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import java.util.List;
import org.springframework.stereotype.Component;

/** Resolves taking the initiative and the resulting venture into the Undercity. */
@Component
public class TakeInitiativeEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TakeInitiativeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.initiativePlayerId = entry.getControllerId();
        StackEntry venture = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                "The initiative's ability",
                List.of(new VentureIntoDungeonEffect(Dungeon.UNDERCITY)),
                0,
                entry.getSourcePermanentId());
        venture.setNonTargeting(true);
        gameData.enqueueTrigger(venture);
    }
}
