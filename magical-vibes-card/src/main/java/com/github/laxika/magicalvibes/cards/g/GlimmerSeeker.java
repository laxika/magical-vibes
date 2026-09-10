package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "14")
public class GlimmerSeeker extends Card {

    public GlimmerSeeker() {
        PermanentAllOfPredicate glimmerCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.GLIMMER)));
        CreateTokenEffect glimmerToken = new CreateTokenEffect(
                "Glimmer", 1, 1, CardColor.WHITE, List.of(CardSubtype.GLIMMER),
                Set.of(), Set.of(CardType.ENCHANTMENT));

        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, new SurvivalTriggerEffect(
                new ConditionalEffect(
                        new SourceIsTapped(),
                        SequenceEffect.of(
                                new ConditionalEffect(new ControlsPermanent(glimmerCreature), new DrawCardEffect()),
                                new ConditionalEffect(new NotCondition(new ControlsPermanent(glimmerCreature)), glimmerToken)))));
    }
}
