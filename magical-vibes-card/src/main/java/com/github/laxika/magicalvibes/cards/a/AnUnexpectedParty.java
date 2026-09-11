package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;

@CardRegistration(set = "HOB", collectorNumber = "29")
public class AnUnexpectedParty extends Card {

    public AnUnexpectedParty() {
        setBackFaceCard(new AtTheDoor());
        addCastingOption(new AdventureCast("{X}{2}{W}"));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenSubtypeEffect(2, 2));
    }

    @Override
    public String getBackFaceClassName() {
        return "AtTheDoor";
    }
}
