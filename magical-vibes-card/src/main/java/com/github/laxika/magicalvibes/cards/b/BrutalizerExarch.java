package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "105")
public class BrutalizerExarch extends Card {

    public BrutalizerExarch() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                "Target must be a noncreature permanent"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Search your library for a creature card, reveal it, then shuffle and put that card on top",
                                new SearchLibraryForCreatureToTopOfLibraryEffect()
                        ),
                        new ChooseOneEffect.ChooseOneOption(
                                "Put target noncreature permanent on the bottom of its owner's library",
                                new PutTargetOnBottomOfLibraryEffect()
                        )
                )));
    }
}
