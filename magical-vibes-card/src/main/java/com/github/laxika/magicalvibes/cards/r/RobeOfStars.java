package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "SPG", collectorNumber = "121")
public class RobeOfStars extends Card {

    public RobeOfStars() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 3, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new PhaseOutEffect(PhaseOutSubject.ATTACHED)),
                "{1}{W}: Equipped creature phases out."
        ));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
