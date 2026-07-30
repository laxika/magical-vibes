package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.LilianaOfTheDarkRealmsEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "97")
public class LilianaOfTheDarkRealms extends Card {

    public LilianaOfTheDarkRealms() {
        // +1: Search your library for a Swamp card, reveal it, put it into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.SWAMP))),
                "+1: Search your library for a Swamp card, reveal it, put it into your hand, then shuffle."
        ));

        // −3: Target creature gets +X/+X or -X/-X until end of turn, where X is the number of Swamps
        // you control. The card is not modal, so the +X/+X vs -X/-X choice is made on resolution —
        // ChooseOneEffect in an activated ability splices the chosen mode into the paused resolution,
        // and both modes pump the creature already chosen by the ability's target filter.
        DynamicAmount swamps = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER);
        DynamicAmount negativeSwamps = new Scaled(swamps, -1);
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Target creature gets +X/+X until end of turn.",
                                new BoostTargetCreatureEffect(swamps, swamps)),
                        new ChooseOneEffect.ChooseOneOption("Target creature gets -X/-X until end of turn.",
                                new BoostTargetCreatureEffect(negativeSwamps, negativeSwamps))))),
                "−3: Target creature gets +X/+X or -X/-X until end of turn, where X is the number of Swamps you control.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        // −6: You get an emblem with "Swamps you control have '{T}: Add {B}{B}{B}{B}.'"
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new LilianaOfTheDarkRealmsEmblemEffect()),
                "−6: You get an emblem with \"Swamps you control have '{T}: Add {B}{B}{B}{B}.'\""
        ));
    }
}
