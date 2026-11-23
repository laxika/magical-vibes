package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEventTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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
        addStaticEffect(DoesntUntapEffect.self());

        // "{T}: Gain control of target creature for as long as you control Merieke Ri Berit. When Merieke
        // Ri Berit leaves the battlefield or becomes untapped, destroy that creature. It can't be
        // regenerated." Each resolution creates its own delayed trigger for its own creature (CR 603.7a).
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new GainControlOfTargetEffect(Duration.WHILE_SOURCE_ON_BATTLEFIELD),
                        RegisterDelayedEventTriggerEffect.aboutWhenSourceLeavesOrUntaps(Subjects.target(),
                                new DestroyTargetPermanentEffect(Subjects.triggering(), true))),
                "{T}: Gain control of target creature for as long as you control Merieke Ri Berit. When Merieke Ri Berit leaves the battlefield or becomes untapped, destroy that creature. It can't be regenerated.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
