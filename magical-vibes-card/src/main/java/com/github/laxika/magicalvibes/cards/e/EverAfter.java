package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SOI", collectorNumber = "109")
public class EverAfter extends Card {

    public EverAfter() {
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), 2, false, false,
                CardColor.BLACK, CardSubtype.ZOMBIE));
        addEffect(EffectSlot.SPELL, new PutSelfOnBottomOfOwnersLibraryEffect());
    }
}
