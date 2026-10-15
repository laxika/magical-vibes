package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DKA", collectorNumber = "128")
public class TrackersInstincts extends Card {

    public TrackersInstincts() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseNToHandRestToGraveyard(
                4, 1, new CardTypePredicate(CardType.CREATURE), true));
        addCastingOption(new FlashbackCast("{2}{U}"));
    }
}
