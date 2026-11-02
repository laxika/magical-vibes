package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsOwnCommander;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "240")
@CardRegistration(set = "PIP", collectorNumber = "190")
@CardRegistration(set = "PIP", collectorNumber = "718")
@CardRegistration(set = "TDC", collectorNumber = "222")
@CardRegistration(set = "AFC", collectorNumber = "132")
@CardRegistration(set = "ONC", collectorNumber = "102")
@CardRegistration(set = "C18", collectorNumber = "23")
@CardRegistration(set = "DRC", collectorNumber = "104")
public class LoyalApprentice extends Card {

    public LoyalApprentice() {
        // At the beginning of combat on your turn, if you control your commander,
        // create a 1/1 colorless Thopter artifact creature token with flying.
        // That token gains haste until end of turn.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new ConditionalEffect(
                new ControllerControlsOwnCommander(),
                new CreateTokenEffect(
                        CardType.CREATURE,
                        1,
                        "Thopter",
                        1,
                        1,
                        null,
                        Set.of(),
                        List.of(CardSubtype.THOPTER),
                        Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT),
                        false,
                        false,
                        List.of(),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of(Keyword.HASTE))));
    }
}
