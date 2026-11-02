package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "503")
public class ThorGuardianOfMidgard extends Card {

    public ThorGuardianOfMidgard() {
        addTrigger(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(),
                new MayEffect(
                        new ExileTopCardMayPlayThisTurnEffect(new EventValue(), false),
                        "Exile that many cards from the top of your library and play them this turn?"));
    }
}
