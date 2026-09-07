package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "RAV", collectorNumber = "172")
public class LifeFromTheLoam extends Card {

    public LifeFromTheLoam() {
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardTypePredicate(CardType.LAND), 3));
        addEffect(EffectSlot.GRAVEYARD_DRAW_REPLACEMENT, new DredgeEffect(3));
    }
}
