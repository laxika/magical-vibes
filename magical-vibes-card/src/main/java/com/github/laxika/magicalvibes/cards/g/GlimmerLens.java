package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONC", collectorNumber = "6")
@CardRegistration(set = "ONC", collectorNumber = "44")
public class GlimmerLens extends Card {

    public GlimmerLens() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LivingWeaponEffect(new CreateTokenEffect("Rebel", 2, 2, CardColor.RED,
                        List.of(CardSubtype.REBEL), Set.of(), Set.of())));
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(
                        new MinimumMatchingAttackers(2, new PermanentIsCreaturePredicate()),
                        new DrawCardEffect()));
        addActivatedAbility(new EquipActivatedAbility("{1}{W}"));
    }
}
