package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "343")
public class MyFollowersAscend extends Card {

    public MyFollowersAscend() {
        PermanentPredicate creature = new PermanentIsCreaturePredicate();
        ControlsPermanent controlsCreature = new ControlsPermanent(creature);

        addEffect(EffectSlot.SPELL,
                ConditionalEffect.unless(controlsCreature,
                        SequenceEffect.of(
                                new PutCounterOnChosenOwnPermanentEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, 5, creature),
                                new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(
                                        Keyword.FLYING, null),
                                new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(
                                        Keyword.VIGILANCE, null))));
        addEffect(EffectSlot.SPELL,
                ConditionalEffect.unless(new NotCondition(controlsCreature),
                        new CreateTokenEffect(
                                "Scarecrow", 4, 4, null,
                                List.of(CardSubtype.SCARECROW), Set.of(Keyword.VIGILANCE),
                                Set.of(CardType.ARTIFACT))));
    }
}
