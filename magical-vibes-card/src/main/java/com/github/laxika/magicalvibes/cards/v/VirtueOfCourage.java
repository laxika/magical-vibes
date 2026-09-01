package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EmberethBlaze;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "WOE", collectorNumber = "157")
public class VirtueOfCourage extends Card {

    public VirtueOfCourage() {
        setBackFaceCard(new EmberethBlaze());
        addCastingOption(new AdventureCast("{1}{R}"));
        addEffect(EffectSlot.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT,
                new MayEffect(
                        new ExileTopCardMayPlayThisTurnEffect(new EventValue(), false),
                        "Exile that many cards from the top of your library and play them this turn?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "EmberethBlaze";
    }
}
