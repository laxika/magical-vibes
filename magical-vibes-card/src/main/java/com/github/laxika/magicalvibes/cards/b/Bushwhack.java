package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "215")
@CardRegistration(set = "BRO", collectorNumber = "174")
public class Bushwhack extends Card {

    public Bushwhack() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a basic land card, reveal it, put it into your hand, then shuffle",
                        new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control fights target creature you don't control",
                        List.of(new FightTargetsEffect()),
                        List.of(
                                new ControlledPermanentPredicateTargetFilter(
                                        new PermanentIsCreaturePredicate(),
                                        "First target must be a creature you control"),
                                new PermanentPredicateTargetFilter(
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentNotPredicate(
                                                        new PermanentControlledBySourceControllerPredicate()))),
                                        "Second target must be a creature you don't control"))
        ))));
    }
}
