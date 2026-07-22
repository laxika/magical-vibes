package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "146")
public class BorrowedHostility extends Card {

    public BorrowedHostility() {
        // Escalate {3} (Pay this cost for each mode chosen beyond the first.)
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{3}"));

        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");

        // Choose one or both — same creature may be chosen for both modes (CR 114.6c).
        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +3/+0 until end of turn",
                        new BoostTargetCreatureEffect(3, 0),
                        creatureFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gains first strike until end of turn",
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET),
                        creatureFilter)
        )));
    }
}
