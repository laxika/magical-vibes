package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyChangeCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSharesColorWithControlledPermanentPredicate;

@CardRegistration(set = "YECL", collectorNumber = "18")
public class CircadianStruggle extends Card {

    public CircadianStruggle() {
        ColorsAmongControlledPermanents colorsAmongControlledPermanents =
                new ColorsAmongControlledPermanents();
        addSpellEffect(new SeekLibraryEffect(colorsAmongControlledPermanents,
                new CardSharesColorWithControlledPermanentPredicate(), LibrarySearchDestination.HAND));
        // "For each color among permanents you control, those cards perpetually gain 'This spell costs {1} less'."
        addSpellEffect(new RepeatEffect(colorsAmongControlledPermanents,
                new PerpetuallyChangeCastCostEffect(Subjects.cardsThisWay(StepKind.SEEK), -1)));
    }
}
