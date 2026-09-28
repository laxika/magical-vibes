package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "29")
public class OctaviaLivingThesis extends Card {

    public OctaviaLivingThesis() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(8, instantOrSorcery),
                new ReduceOwnCastCostEffect(new Fixed(8))));

        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(8));

        List<CardEffect> setBasePowerToughness = List.of(new SetBasePowerToughnessEffect(8, 8));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(instantOrSorcery, setBasePowerToughness,
                        null, TargetFilters.creature()));
        addEffect(EffectSlot.ON_CONTROLLER_COPIES_SPELL,
                new SpellCopyTriggerEffect(instantOrSorcery, setBasePowerToughness,
                        TargetFilters.creature()));
    }
}
