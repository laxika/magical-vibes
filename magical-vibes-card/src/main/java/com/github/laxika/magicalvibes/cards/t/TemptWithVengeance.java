package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferCreateTokensEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C13", collectorNumber = "125")
public class TemptWithVengeance extends Card {

    public TemptWithVengeance() {
        addEffect(EffectSlot.SPELL, new TemptingOfferCreateTokensEffect(
                new CreateTokenEffect(new XValue(), "Elemental", 1, 1, CardColor.RED,
                        List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE), Set.of())));
    }
}
