package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "271")
public class SurtlandFrostpyre extends Card {

    public SurtlandFrostpyre() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {R}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        // {2}{U}{U}{R}, {T}, Sacrifice this land: Scry 2. This land deals 2 damage to each creature.
        // Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}{U}{R}",
                List.of(
                        new SacrificeSelfCost(),
                        new ScryEffect(2),
                        new DealDamageToEachMatchingPermanentEffect(
                                2, new PermanentIsCreaturePredicate(), EachPermanentScope.ALL_PLAYERS)
                ),
                "{2}{U}{U}{R}, {T}, Sacrifice Surtland Frostpyre: Scry 2. This land deals 2 damage to each creature. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
