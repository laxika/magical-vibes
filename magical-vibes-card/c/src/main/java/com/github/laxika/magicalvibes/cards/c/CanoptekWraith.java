package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.AllOfCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "153")
public class CanoptekWraith extends Card {

    public CanoptekWraith() {
        addStaticEffect(new CantBeBlockedEffect());
        // "Pay {3} and sacrifice it" can't be paid once it has left the battlefield (CR 400.7). "Choose a land you
        // control. Then search your library for up to two basic land cards which have the same name as the chosen
        // land": with no land to choose the search still happens, finding nothing.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), UnlessPaysEffect.youMayPay(
                AllOfCost.of(new PayManaCost("{3}"), new SacrificeSelfCost()), SequenceEffect.of(
                        ChoosePermanentsEffect.exactly(1, Subjects.landsYouControl(), "a land you control"),
                        new SearchLibraryEffect(new Fixed(2), new CardAllOfPredicate(List.of(
                                CardPredicateUtils.basicLand(),
                                new CardSharesNameWithPermanentsPredicate(Subjects.thisWay(StepKind.CHOOSE)))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED))));
    }
}
