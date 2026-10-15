package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NihiloorAttackEffect;
import com.github.laxika.magicalvibes.model.effect.NihiloorEnterEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;

@CardRegistration(set = "AFC", collectorNumber = "53")
public class Nihiloor extends Card {

    public Nihiloor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new NihiloorEnterEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate()),
                new NihiloorAttackEffect()));
    }
}
