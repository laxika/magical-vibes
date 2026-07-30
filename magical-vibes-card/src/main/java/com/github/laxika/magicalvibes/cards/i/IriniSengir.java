package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "54")
public class IriniSengir extends Card {

    public IriniSengir() {
        // Green enchantment spells and white enchantment spells cost {2} more to cast.
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.GREEN),
                                new CardColorPredicate(CardColor.WHITE))))),
                2));
    }
}
