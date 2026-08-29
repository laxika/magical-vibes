package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfCardsInAllGraveyardsSelfEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfCardsInAllGraveyardsEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        GainActivatedAbilitiesOfCardsInAllGraveyardsEffect gainEffect =
                (GainActivatedAbilitiesOfCardsInAllGraveyardsEffect) effect;
        GameData gameData = context.gameData();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (!card.hasType(gainEffect.cardType())) continue;
                for (var ability : card.getActivatedAbilities()) {
                    accumulator.addActivatedAbility(ability);
                }
                List<CardEffect> onTapEffects = card.getEffects(EffectSlot.ON_TAP);
                if (!onTapEffects.isEmpty()) {
                    accumulator.addActivatedAbility(new ActivatedAbility(
                            true,
                            null,
                            onTapEffects,
                            "{T}: Add mana."
                    ));
                }
            }
        }
    }
}
