package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "12")
@CardRegistration(set = "Q06", collectorNumber = "5")
@CardRegistration(set = "PIO", collectorNumber = "328")
public class HiddenStrings extends Card {

    public HiddenStrings() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.SPELL, tapOrUntapTarget("You may tap or untap the first target permanent?"));
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.SPELL, tapOrUntapTarget("You may tap or untap the second target permanent?"));
        addEffect(EffectSlot.SPELL,
                new MayEffect(new CipherEncodeEffect(), "Encode this spell on a creature you control?"));
    }

    private static CardEffect tapOrUntapTarget(String prompt) {
        return new MayEffect(new ChooseOneForTargetPermanentEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Tap",
                        List.of(new TapPermanentsEffect(TapUntapScope.TARGET))),
                new ChooseOneEffect.ChooseOneOption("Untap",
                        List.of(new UntapPermanentsEffect(TapUntapScope.TARGET))))), prompt);
    }
}
