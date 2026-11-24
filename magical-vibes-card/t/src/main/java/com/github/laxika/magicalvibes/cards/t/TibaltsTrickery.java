package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithPermanentsPredicate;

import java.util.List;

import static com.github.laxika.magicalvibes.model.effect.Subjects.cardsThisWay;
import static com.github.laxika.magicalvibes.model.effect.Subjects.controllerOf;
import static com.github.laxika.magicalvibes.model.effect.Subjects.targetSpell;

@CardRegistration(set = "KHM", collectorNumber = "153")
@CardRegistration(set = "KHM", collectorNumber = "360")
public class TibaltsTrickery extends Card {

    public TibaltsTrickery() {
        PlayerSubject itsController = controllerOf(targetSpell());
        // A face-down spell has no name, so every card with one has a different name (CR 201.2c).
        CardPredicate differentNameNonland = new CardAllOfPredicate(List.of(ExileCardsFromTopUntilEffect.NONLAND,
                new CardNotPredicate(new CardSharesNameWithPermanentsPredicate(targetSpell()))));
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new ChooseOneAtRandomEffect(List.of(
                new ChooseOneOption("Mill 1", new MillEffect(1, itsController)),
                new ChooseOneOption("Mill 2", new MillEffect(2, itsController)),
                new ChooseOneOption("Mill 3", new MillEffect(3, itsController)))));
        addSpellEffect(new ExileCardsFromTopUntilEffect(itsController, differentNameNonland));
        addSpellEffect(new MayCastDiscardedCardWithoutPayingManaCostEffect(cardsThisWay(StepKind.EXILE),
                differentNameNonland).castBy(itsController));
        addSpellEffect(PutCardsIntoLibraryEffect.onBottomInRandomOrder(cardsThisWay(StepKind.EXILE)));
    }
}
