package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "29")
@CardRegistration(set = "OTC", collectorNumber = "103")
public class OctaviaLivingThesis extends Card {

    public OctaviaLivingThesis() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addStaticEffect(new ConditionalEffect(
                new GraveyardCardThreshold(8, instantOrSorcery),
                new ReduceOwnCastCostEffect(new Fixed(8))));

        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysEffect(8));

        List<CardEffect> setBasePowerToughness = List.of(new SetBasePowerToughnessEffect(8, 8));
        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery),
                setBasePowerToughness.toArray(CardEffect[]::new));
        addTrigger(Triggers.youCopySpell(),
                new SpellCopyTriggerEffect(instantOrSorcery, setBasePowerToughness,
                        TargetFilters.creature()));
    }
}
