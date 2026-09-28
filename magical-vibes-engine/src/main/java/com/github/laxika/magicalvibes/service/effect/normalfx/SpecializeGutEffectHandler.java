package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.g.GutFanaticalPriestess;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeGutEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SpecializeGutEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeGutEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SpecializeGutEffect specialize = (SpecializeGutEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Gut, Fanatical Priestess".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        GutFanaticalPriestess.setBaseFaceCharacteristics(specialized);
        GutFanaticalPriestess.setFaceCharacteristics(specialized, specialize.color());
        source.exchangeCard(specialized);

        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                specialized,
                entry.getControllerId(),
                specialized.getName() + "'s ability",
                List.of(GutFanaticalPriestess.specializedTrigger(specialize.color())),
                0,
                entry.getSourcePermanentId());
        trigger.setNonTargeting(true);
        gameData.enqueueTrigger(trigger);
    }
}
