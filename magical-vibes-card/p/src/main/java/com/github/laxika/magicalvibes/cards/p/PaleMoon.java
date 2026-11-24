package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "NEM", collectorNumber = "36")
public class PaleMoon extends Card {

    public PaleMoon() {
        addSpellEffect(new ReplaceLandManaWithColorEffect(ManaColor.COLORLESS)
                .forLands(new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC)))
                .lasting(Duration.UNTIL_END_OF_TURN));
    }
}
