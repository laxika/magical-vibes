package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "227")
public class AngrathCaptainOfChaos extends Card {

    public AngrathCaptainOfChaos() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.MENACE, GrantScope.OWN_CREATURES));

        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        ControlsPermanent controlsArmy = new ControlsPermanent(army);

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        ConditionalEffect.unless(controlsArmy,
                                SequenceEffect.of(
                                        new PutCounterOnChosenOwnPermanentEffect(
                                                CounterType.PLUS_ONE_PLUS_ONE, 2, army),
                                        new GrantSubtypeToChosenPermanentEffect(CardSubtype.ZOMBIE))),
                        ConditionalEffect.unless(new NotCondition(controlsArmy),
                                SequenceEffect.of(
                                        new CreateTokenEffect("Zombie Army", 0, 0, CardColor.BLACK,
                                                List.of(CardSubtype.ZOMBIE, CardSubtype.ARMY), Set.of(), Set.of()),
                                        new PutCounterOnChosenOwnPermanentEffect(
                                                CounterType.PLUS_ONE_PLUS_ONE, 2, army)))
                ),
                "\u22122: Amass Zombies 2."
        ));
    }
}
