package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "139")
@CardRegistration(set = "5ED", collectorNumber = "165")
public class Gloom extends Card {

    public Gloom() {
        // White spells cost {3} more to cast.
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardColorPredicate(CardColor.WHITE), 3));

        // Activated abilities of white enchantments cost {3} more to activate.
        addEffect(EffectSlot.STATIC, new IncreaseActivatedAbilityCostEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                        new PermanentIsEnchantmentPredicate())),
                3));
    }
}
