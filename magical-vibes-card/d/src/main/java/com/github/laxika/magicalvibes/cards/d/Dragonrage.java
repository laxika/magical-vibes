package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "97")
public class Dragonrage extends Card {

    public Dragonrage() {
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.RED,
                new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.CONTROLLER)));
        addEffect(EffectSlot.SPELL, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{R}",
                        List.of(new BoostSelfEffect(1, 0)),
                        "{R}: This creature gets +1/+0 until end of turn."
                ),
                GrantScope.OWN_CREATURES,
                new PermanentIsAttackingPredicate(),
                EffectDuration.UNTIL_END_OF_TURN
        ));
    }
}
