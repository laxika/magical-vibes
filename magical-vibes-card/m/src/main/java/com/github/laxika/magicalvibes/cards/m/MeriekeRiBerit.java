package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DestroyLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "297")
@CardRegistration(set = "TSB", collectorNumber = "95")
public class MeriekeRiBerit extends Card {

    public MeriekeRiBerit() {
        // "Merieke Ri Berit doesn't untap during your untap step."
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        // "{T}: Gain control of target creature for as long as you control Merieke Ri Berit."
        // The stolen creature is linked to Merieke so the destroy triggers below can still refer
        // to it after the control effect has ended.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(GainControlOfTargetEffect.linkingToSource(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD)),
                "{T}: Gain control of target creature for as long as you control Merieke Ri Berit.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));

        // "When Merieke Ri Berit leaves the battlefield or becomes untapped, destroy that
        // creature. It can't be regenerated."
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new DestroyLinkedPermanentEffect(true));
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new DestroyLinkedPermanentEffect(true));
    }
}
