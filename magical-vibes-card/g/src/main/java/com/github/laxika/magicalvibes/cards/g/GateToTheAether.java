package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "174")
public class GateToTheAether extends Card {

    public GateToTheAether() {
        var match = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT), new CardTypePredicate(CardType.CREATURE), new CardTypePredicate(CardType.ENCHANTMENT), new CardTypePredicate(CardType.LAND)));
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new RevealTopCardOfLibraryEffect(Subjects.triggeringPlayer()),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, match),
                        new MayEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(match)),
                                "Put the revealed card onto the battlefield?", null, MayChoicePlayer.ACTIVE_PLAYER)));
    }
}
