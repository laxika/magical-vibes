package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorAtLeastCardsInLibrary;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

@CardRegistration(set = "THB", collectorNumber = "73")
public class ThassasOracle extends Card {

    public ThassasOracle() {
        ColorManaSymbolsAmongControlledPermanents blueDevotion =
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLUE);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new LookAtTopCardsEffect(
                        blueDevotion, new Fixed(1), null,
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                        LibrarySearchDestination.TOP_OF_LIBRARY, true),
                ConditionalEffect.unless(
                        new DevotionToColorAtLeastCardsInLibrary(ManaColor.BLUE),
                        new WinGameEffect())));
    }
}
