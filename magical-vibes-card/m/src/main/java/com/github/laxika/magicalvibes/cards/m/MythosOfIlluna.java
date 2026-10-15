package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;

@CardRegistration(set = "IKO", collectorNumber = "58")
public class MythosOfIlluna extends Card {

    public MythosOfIlluna() {
        ConditionalEffect fightEffect = new ConditionalEffect(
                new SourceIsCreature(), new EnteringCreatureFightsTargetCreatureEffect());
        CreateTokenCopyOfTargetPermanentEffect enhancedCopy =
                CreateTokenCopyOfTargetPermanentEffect.withAdditionalEffects(
                        false, Map.of(EffectSlot.ON_ENTER_BATTLEFIELD, List.of(fightEffect)));

        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new AllConditions(List.of(
                        new ColorSpentToCast(ManaColor.RED),
                        new ColorSpentToCast(ManaColor.GREEN))),
                new CreateTokenCopyOfTargetPermanentEffect(), enhancedCopy));
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, fightEffect);
    }
}
