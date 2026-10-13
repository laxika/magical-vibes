package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "114")
public class FodderLaunch extends Card {

    public FodderLaunch() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentCost(
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                "Sacrifice a Goblin"
        ));
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-5, -5));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(5, DamageRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
