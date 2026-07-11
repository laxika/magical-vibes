package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestManaValueAmongAllCreaturesPredicate;

import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "14")
public class FavorOfTheMighty extends Card {

    public FavorOfTheMighty() {
        // Each creature with the greatest mana value has protection from each color.
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new ProtectionFromColorsEffect(Set.of(
                        CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN)),
                GrantScope.ALL_CREATURES,
                new PermanentHasGreatestManaValueAmongAllCreaturesPredicate()));
    }
}
