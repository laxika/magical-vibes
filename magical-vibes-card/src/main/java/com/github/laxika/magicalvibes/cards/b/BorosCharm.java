package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "148")
public class BorosCharm extends Card {

    public BorosCharm() {
        // Choose one — modes 0 and 2 target, so each declares its own per-mode target filter;
        // mode 1 is a non-targeting mass grant.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Boros Charm deals 4 damage to target player or planeswalker",
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(4),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsPlaneswalkerPredicate(),
                                "Target must be a player or planeswalker."
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Permanents you control gain indestructible until end of turn",
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS)),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gains double strike until end of turn",
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature."
                        )
                )
        )));
    }
}
