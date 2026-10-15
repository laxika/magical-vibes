package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2363")
public class PiratedCopy extends Card {

    public PiratedCopy() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(),
                "creature",
                Set.of(CardSubtype.PIRATE),
                Map.of(EffectSlot.ON_CREATURE_WITH_SAME_NAME_COMBAT_DAMAGE_TO_PLAYER,
                        List.of(new AllyCombatDamageTriggerEffect(
                                new PermanentHasSameNameAsSourcePredicate(),
                                new DrawCardEffect(1))))));
    }
}
