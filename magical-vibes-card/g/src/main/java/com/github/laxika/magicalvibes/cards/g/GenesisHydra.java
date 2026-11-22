package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "176")
@CardRegistration(set = "IMA", collectorNumber = "163")
@CardRegistration(set = "MOC", collectorNumber = "298")
@CardRegistration(set = "OTC", collectorNumber = "193")
@CardRegistration(set = "C20", collectorNumber = "172")
@CardRegistration(set = "NEC", collectorNumber = "118")
public class GenesisHydra extends Card {

    public GenesisHydra() {
        addTrigger(Triggers.youCastThisSpell(), RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new XValue()),
                ChooseCardEffect.upTo(Subjects.revealedThisWay(new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(), new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                                new CardMaxManaValueXPredicate()))), 1,
                        "a nonland permanent card with mana value X or less to put onto the battlefield"),
                new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                new ShuffleLibraryEffect(false));
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
