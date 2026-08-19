package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "90")
public class VonasHunger extends Card {

    public VonasHunger() {
        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();
        addEffect(EffectSlot.SPELL, new AscendEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerHasCityBlessing(),
                new SacrificePermanentsEffect(
                        new HalvedRoundedUp(new PermanentCount(creature, CountScope.CONTROLLER)),
                        new PermanentAnyOfPredicate(List.of(creature)), SacrificeRecipient.EACH_OPPONENT, true)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(new ControllerHasCityBlessing()),
                new SacrificePermanentsEffect(1, creature, SacrificeRecipient.EACH_OPPONENT)));
    }
}
