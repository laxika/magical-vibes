package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EmberethBlaze;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "157")
@CardRegistration(set = "WOE", collectorNumber = "282")
public class VirtueOfCourage extends Card {

    public VirtueOfCourage() {
        setBackFaceCard(new EmberethBlaze());
        addCastingOption(new AdventureCast("{1}{R}"));
        addTrigger(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(),
                new MayEffect(
                        new ExileTopCardMayPlayThisTurnEffect(new EventValue(), false),
                        "Exile that many cards from the top of your library and play them this turn?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "EmberethBlaze";
    }
}
