package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "128")
public class FireIce extends Card {

    public FireIce() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Fire — Fire deals 2 damage divided as you choose among one or two targets",
                        List.<CardEffect>of(DealDividedDamageEffect.chosenAmongAnyTargets(2)),
                        null, null, 1, 2, false, null
                ).withManaCost("{1}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Ice — Tap target permanent. Draw a card",
                        List.of(new TapPermanentsEffect(TapUntapScope.TARGET), new DrawCardEffect(1)),
                        TargetFilters.permanent()
                ).withManaCost("{1}{U}")
        )));
    }
}
