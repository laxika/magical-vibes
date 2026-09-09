package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "136")
@CardRegistration(set = "H09", collectorNumber = "10")
public class QuickSliver extends Card {

    public QuickSliver() {
        addEffect(EffectSlot.STATIC,
                new GrantFlashToCardTypeEffect(new CardSubtypePredicate(CardSubtype.SLIVER), true));
    }
}
