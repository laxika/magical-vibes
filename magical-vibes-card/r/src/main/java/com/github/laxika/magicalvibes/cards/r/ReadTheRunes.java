package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisResolution;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "ONS", collectorNumber = "104")
@CardRegistration(set = "C16", collectorNumber = "95")
public class ReadTheRunes extends Card {

    public ReadTheRunes() {
        addSpellEffect(new DrawCardEffect(new XValue()));
        // For each card drawn this way, discard a card unless you sacrifice a permanent: you are never forced to
        // sacrifice; you sacrifice the permanents you want, then discard the rest (its rulings).
        addSpellEffect(new SacrificePermanentsEffect(new CardsDrawnThisResolution(), new PermanentTruePredicate(),
                Subjects.you(), false, false, false, false, true));
        addSpellEffect(new DiscardEffect(new Max(new Fixed(0), new Sum(new CardsDrawnThisResolution(),
                new Scaled(new AmountThisWay(StepKind.SACRIFICE), -1))), Subjects.you()));
    }
}
