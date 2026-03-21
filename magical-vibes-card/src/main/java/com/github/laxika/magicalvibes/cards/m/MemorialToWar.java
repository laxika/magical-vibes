package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "246")
public class MemorialToWar extends Card {

    public MemorialToWar() {
        // Memorial to War enters the battlefield tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        // {T}: Add {R}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.RED));
        // {4}{R}, {T}, Sacrifice Memorial to War: Destroy target land.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{R}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{4}{R}, {T}, Sacrifice Memorial to War: Destroy target land.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Target must be a land"
                )
        ));
    }
}
