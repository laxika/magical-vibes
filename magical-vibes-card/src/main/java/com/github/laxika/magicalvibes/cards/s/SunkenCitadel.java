package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "285")
@CardRegistration(set = "LCI", collectorNumber = "392")
public class SunkenCitadel extends Card {

    public SunkenCitadel() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardChosenColorManaEffect()),
                "{T}: Add one mana of the chosen color."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardChosenColorManaEffect(new ManaRestriction.LandAbilities()),
                        new AwardChosenColorManaEffect(new ManaRestriction.LandAbilities())),
                "{T}: Add two mana of the chosen color. Spend this mana only to activate abilities of land sources."
        ));
    }
}
