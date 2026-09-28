package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "108")
public class YoureConfrontedByRobbers extends Card {

    public YoureConfrontedByRobbers() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Stall for Time — Tap up to three target creatures",
                        List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                        TargetFilters.creature(), null, 0, 3, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Call for Aid — Create three 1/1 white Soldier creature tokens",
                        CreateTokenEffect.whiteSoldier(3))
        )));
    }
}
