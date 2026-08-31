package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllLandsBecomeBasicLandTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DIS", collectorNumber = "49")
public class Nightcreep extends Card {

    public Nightcreep() {
        addEffect(EffectSlot.SPELL,
                new GrantColorUntilEndOfTurnEffect(CardColor.BLACK, GrantScope.ALL_CREATURES));
        addEffect(EffectSlot.SPELL,
                new AllLandsBecomeBasicLandTypeUntilEndOfTurnEffect(CardSubtype.SWAMP));
    }
}
