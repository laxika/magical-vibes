package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SCG", collectorNumber = "127")
public class RootElemental extends Card {

    public RootElemental() {
        addMorph("{5}{G}{G}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new MayEffect(
                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"),
                "Put a creature card from your hand onto the battlefield?"));
    }
}
