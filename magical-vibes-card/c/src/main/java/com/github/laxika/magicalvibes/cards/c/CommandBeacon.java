package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsCommanderPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "470")
@CardRegistration(set = "SLD", collectorNumber = "1533")
@CardRegistration(set = "FCA", collectorNumber = "64")
@CardRegistration(set = "C15", collectorNumber = "56")
@CardRegistration(set = "TDC", collectorNumber = "352")
@CardRegistration(set = "OTC", collectorNumber = "279")
public class CommandBeacon extends Card {

    public CommandBeacon() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        ChooseCardEffect.oneOf(Subjects.cardsInYourCommandZone(new CardIsCommanderPredicate()),
                                "a commander to put into your hand"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))),
                "{T}, Sacrifice Command Beacon: Put your commander into your hand from the command zone."
        ));
    }
}
