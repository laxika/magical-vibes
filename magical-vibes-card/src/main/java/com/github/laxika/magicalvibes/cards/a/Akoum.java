package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OPC2", collectorNumber = "9")
public class Akoum extends Card {

    public Akoum() {
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(
                new CardTypePredicate(CardType.ENCHANTMENT), true));

        PermanentPredicate nonEnchantedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsEnchantedPredicate())));
        target(new PermanentPredicateTargetFilter(
                nonEnchantedCreature,
                "Target must be a creature that isn't enchanted"))
                .addEffect(EffectSlot.CHAOS_TRIGGERED,
                        new DestroyTargetPermanentEffect(nonEnchantedCreature));
    }
}
