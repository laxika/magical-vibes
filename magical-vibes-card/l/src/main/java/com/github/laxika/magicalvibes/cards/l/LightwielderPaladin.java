package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "19")
@CardRegistration(set = "E01", collectorNumber = "14")
public class LightwielderPaladin extends Card {

    public LightwielderPaladin() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new ExilePermanentDamagedPlayerControlsEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED))),
                        "You may exile target black or red permanent that player controls."));
    }
}
