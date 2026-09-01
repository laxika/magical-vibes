package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "62")
public class LostDays extends Card {

    public LostDays() {
        var creatureOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsEnchantmentPredicate()));
        target(new PermanentPredicateTargetFilter(
                creatureOrEnchantment,
                "Target must be a creature or enchantment"))
                .addEffect(EffectSlot.SPELL,
                        new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(1, creatureOrEnchantment))
                .addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(1));
    }
}
