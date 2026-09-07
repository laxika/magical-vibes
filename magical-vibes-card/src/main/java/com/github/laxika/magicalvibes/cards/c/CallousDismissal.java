package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "44")
public class CallousDismissal extends Card {

    public CallousDismissal() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        ControlsPermanent controlsArmy = new ControlsPermanent(army);

        addEffect(EffectSlot.SPELL,
                ConditionalEffect.unless(controlsArmy,
                        SequenceEffect.of(
                                new PutCounterOnChosenOwnPermanentEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1, army),
                                new GrantSubtypeToChosenPermanentEffect(CardSubtype.ZOMBIE))));
        addEffect(EffectSlot.SPELL,
                ConditionalEffect.unless(new NotCondition(controlsArmy),
                        new CreateTokenEffect(
                                CardType.CREATURE, 1, "Zombie Army", 0, 0, CardColor.BLACK, null,
                                List.of(CardSubtype.ZOMBIE, CardSubtype.ARMY), Set.of(), Set.of(),
                                false, false, Map.of(), List.of(), false, false, false, 1, Set.of())));
    }
}
