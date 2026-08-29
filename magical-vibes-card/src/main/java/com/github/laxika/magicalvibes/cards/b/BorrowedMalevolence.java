package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "82")
public class BorrowedMalevolence extends Card {

    public BorrowedMalevolence() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}"));

        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");

        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +1/+1 until end of turn",
                        new BoostTargetCreatureEffect(1, 1),
                        creatureFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -1/-1 until end of turn",
                        new BoostTargetCreatureEffect(-1, -1),
                        creatureFilter)
        )));
    }
}
