package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandThenEachOpponentLosesLifeEffect;
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
                new RevealTopCardPutIntoHandThenEachOpponentLosesLifeEffect());
    }
}
