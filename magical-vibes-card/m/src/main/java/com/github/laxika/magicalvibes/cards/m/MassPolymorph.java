package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M11", collectorNumber = "64")
public class MassPolymorph extends Card {

    public MassPolymorph() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addSpellEffect(new ExileTargetPermanentEffect(Subjects.creaturesYouControl()));
        addSpellEffect(new ConditionalEffect(HappenedThisWay.exiled(),
                SequenceEffect.of(
                        new RevealCardsFromTopUntilEffect(Subjects.you(), creatureCard, new AmountThisWay(StepKind.EXILE)),
                        new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creatureCard)),
                        new ShuffleLibraryEffect(false))));
    }
}
