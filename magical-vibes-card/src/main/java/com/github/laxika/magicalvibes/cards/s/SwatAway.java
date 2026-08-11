package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CreatureAttackingController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "ECL", collectorNumber = "75")
public class SwatAway extends Card {

    public SwatAway() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CreatureAttackingController(),
                new ReduceOwnCastCostEffect(new Fixed(2))));
        addEffect(EffectSlot.SPELL, new PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect());
    }
}
