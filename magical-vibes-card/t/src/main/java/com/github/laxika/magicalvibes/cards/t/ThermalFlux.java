package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "49")
public class ThermalFlux extends Card {

    public ThermalFlux() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target nonsnow permanent becomes snow until end of turn",
                        new GrantSupertypeUntilEndOfTurnEffect(
                                CardSupertype.SNOW, GrantScope.TARGET, true),
                        new PermanentPredicateTargetFilter(
                                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.SNOW)),
                                "Target must be a nonsnow permanent.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Target snow permanent isn't snow until end of turn",
                        new GrantSupertypeUntilEndOfTurnEffect(
                                CardSupertype.SNOW, GrantScope.TARGET, false),
                        new PermanentPredicateTargetFilter(
                                new PermanentHasSupertypePredicate(CardSupertype.SNOW),
                                "Target must be a snow permanent."))
        )));
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
