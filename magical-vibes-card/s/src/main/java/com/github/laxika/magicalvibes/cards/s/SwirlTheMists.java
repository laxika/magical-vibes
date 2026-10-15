package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllColorWordsBecomeChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;

@CardRegistration(set = "CHK", collectorNumber = "94")
public class SwirlTheMists extends Card {

    public SwirlTheMists() {
        // "As this enchantment enters, choose a color word."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());

        // "All instances of color words in the text of spells and permanents are changed to the
        // chosen color word."
        addEffect(EffectSlot.STATIC, new AllColorWordsBecomeChosenColorEffect());
    }
}
