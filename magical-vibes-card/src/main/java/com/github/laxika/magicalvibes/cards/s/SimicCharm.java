package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "195")
public class SimicCharm extends Card {

    public SimicCharm() {
        // Choose one — modes 0 and 2 target a creature; mode 1 is a non-targeting mass grant.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +3/+3 until end of turn",
                        new BoostTargetCreatureEffect(3, 3),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature."
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Permanents you control gain hexproof until end of turn",
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_PERMANENTS)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature to its owner's hand",
                        ReturnToHandEffect.target(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature."
                        )
                )
        )));
    }
}
