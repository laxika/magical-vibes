package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FlameheartWerewolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "SOI", collectorNumber = "169")
public class KessigForgemaster extends Card {

    public KessigForgemaster() {
        setBackFaceCard(new FlameheartWerewolf());

        DealDamageToTargetCreatureEffect damage = new DealDamageToTargetCreatureEffect(1);
        addEffect(EffectSlot.ON_BLOCK, damage);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, damage, TriggerMode.PER_BLOCKER);

        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "FlameheartWerewolf";
    }
}
