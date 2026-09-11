package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerCreatesTokensEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "OPC2", collectorNumber = "24")
public class LairOfTheAshenIdol extends Card {

    public LairOfTheAshenIdol() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "a creature"),
                List.of(new PlaneswalkEffect())));
        target(0, 99).addEffect(EffectSlot.CHAOS_TRIGGERED,
                new EachTargetPlayerCreatesTokensEffect(CreateTokenEffect.blackZombie(1)));
    }
}
