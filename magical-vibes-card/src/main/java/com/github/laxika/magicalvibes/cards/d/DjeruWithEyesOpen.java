package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamageToPlaneswalkersYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HOU", collectorNumber = "10")
public class DjeruWithEyesOpen extends Card {

    public DjeruWithEyesOpen() {
        // "When Djeru enters, you may search your library for a planeswalker card, reveal it, put it
        // into your hand, then shuffle."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(new CardTypePredicate(CardType.PLANESWALKER)),
                        "Search your library for a planeswalker card?"));

        // "If a source would deal damage to a planeswalker you control, prevent 1 of that damage."
        addEffect(EffectSlot.STATIC, new PreventFixedDamageToPlaneswalkersYouControlEffect(1));
    }
}
