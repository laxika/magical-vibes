package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectChosenColorSpellDamageToControllerEffect;

@CardRegistration(set = "INV", collectorNumber = "19")
public class HarshJudgment extends Card {

    public HarshJudgment() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addStaticEffect(new RedirectChosenColorSpellDamageToControllerEffect());
    }
}
