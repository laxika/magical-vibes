package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CastCardWithoutPayingManaCostIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "160")
public class WildEvocation extends Card {

    public WildEvocation() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), SequenceEffect.of(
                RevealCardsFromHandEffect.atRandom(Subjects.triggeringPlayer(), 1),
                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(new CardTypePredicate(CardType.LAND))),
                new CastCardWithoutPayingManaCostIfAbleEffect(Subjects.revealedThisWay())));
    }
}
