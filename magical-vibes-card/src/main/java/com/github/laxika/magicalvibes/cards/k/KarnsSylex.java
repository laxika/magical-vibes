package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPayLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "234")
public class KarnsSylex extends Card {

    public KarnsSylex() {
        // Karn's Sylex enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // Players can't pay life to cast spells or to activate abilities that aren't mana abilities.
        addEffect(EffectSlot.STATIC, new PlayersCantPayLifeEffect());

        // {X}, {T}, Exile Karn's Sylex: Destroy each nonland permanent with mana value X or less.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        new ExileSelfCost(),
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentMaxManaValueXPredicate())))),
                "{X}, {T}, Exile Karn's Sylex: Destroy each nonland permanent with mana value X or less. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
