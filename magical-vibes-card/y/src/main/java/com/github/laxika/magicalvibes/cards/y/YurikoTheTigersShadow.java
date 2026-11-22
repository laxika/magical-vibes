package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FCA", collectorNumber = "60")
@CardRegistration(set = "CMM", collectorNumber = "364")
@CardRegistration(set = "CMM", collectorNumber = "597")
@CardRegistration(set = "CMM", collectorNumber = "690")
@CardRegistration(set = "C18", collectorNumber = "52")
public class YurikoTheTigersShadow extends Card {

    public YurikoTheTigersShadow() {
        addNinjutsu("{U}{B}");

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.NINJA)),
                SequenceEffect.of(
                        new RevealTopCardOfLibraryEffect(Subjects.you()),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.REVEAL)),
                        new LoseLifeEffect(AmountThisWay.revealedManaValue(), Subjects.eachOpponent())));
    }
}
