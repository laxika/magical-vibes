package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SLD", collectorNumber = "1845")
@CardRegistration(set = "LTC", collectorNumber = "227")
public class ShinyImpetus extends Card {

    public ShinyImpetus() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new AttachedBoostEffect(new Fixed(2), new Fixed(2), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GoadCreaturesUntilNextTurnEffect(new PermanentIsHostOfSourceAuraPredicate()))
                .addEffect(EffectSlot.ON_ATTACK, CreateTokenEffect.ofTreasureToken(1));
    }
}
