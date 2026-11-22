package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostControlledLandsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "134")
@CardRegistration(set = "HOC", collectorNumber = "134")
public class PartInFriendship extends Card {

    public PartInFriendship() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new RevealCardsFromTopUntilEffect(creatureCard),
                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(new CardAllOfPredicate(List.of(
                        creatureCard, new CardManaValueAtMostControlledLandsPredicate())))),
                new PutCardsIntoHandEffect(Subjects.revealedThisWay(new CardAllOfPredicate(List.of(creatureCard,
                        new CardNotPredicate(new CardManaValueAtMostControlledLandsPredicate()))))),
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.revealedThisWay()));
    }
}
