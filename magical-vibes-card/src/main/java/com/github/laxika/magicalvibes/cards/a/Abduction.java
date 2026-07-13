package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "6ED", collectorNumber = "55")
public class Abduction extends Card {

    public Abduction() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
        // When this Aura enters, untap enchanted creature.
        .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new UntapPermanentsEffect(TapUntapScope.TARGET));

        // You control enchanted creature.
        addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());

        // When enchanted creature dies, return that card to the battlefield under its owner's control.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect());
    }
}
