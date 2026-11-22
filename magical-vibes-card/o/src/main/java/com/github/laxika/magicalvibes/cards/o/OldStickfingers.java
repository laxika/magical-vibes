package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "234")
@CardRegistration(set = "DBL", collectorNumber = "234")
@CardRegistration(set = "DSC", collectorNumber = "227")
public class OldStickfingers extends Card {

    public OldStickfingers() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addTrigger(Triggers.youCastThisSpell(),
                new RevealCardsFromTopUntilEffect(Subjects.you(), creatureCard, new XValue()),
                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay(creatureCard)),
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.revealedThisWay(new CardNotPredicate(creatureCard))));

        CardsInGraveyard creatureCards = new CardsInGraveyard(
                creatureCard, CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(creatureCards, creatureCards));
    }
}
