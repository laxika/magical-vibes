package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "190")
@CardRegistration(set = "C16", collectorNumber = "156")
public class LurkingPredators extends Card {

    public LurkingPredators() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addTrigger(Triggers.opponentCastsSpell(), new RevealTopCardOfLibraryEffect(Subjects.you()),
                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creatureCard)),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, new CardNotPredicate(creatureCard)),
                        new MayEffect(PutCardsIntoLibraryEffect.onBottom(Subjects.revealedThisWay()),
                        "Put the revealed card on the bottom of your library?")));
    }
}
