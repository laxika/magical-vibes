package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "OHOP", collectorNumber = "38")
public class TurriIsland extends Card {

    public TurriIsland() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.CREATURE), 2, CostModificationScope.ALL));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                LookAtTopCardsEffect.chooseNToHandRestToGraveyard(
                        3, 3, new CardTypePredicate(CardType.CREATURE), true));
    }
}
