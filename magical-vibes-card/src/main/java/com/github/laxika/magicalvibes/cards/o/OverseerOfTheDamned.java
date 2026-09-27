package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C14", collectorNumber = "28")
public class OverseerOfTheDamned extends Card {

    public OverseerOfTheDamned() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target creature?"));
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        CreateTokenEffect.blackZombie(1).withTapped(true)));
    }
}
