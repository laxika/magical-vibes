package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsDistinctPermanentNamesCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "152")
public class MazesEnd extends Card {

    public MazesEnd() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new ReturnSelfToHandCost(),
                        new SearchLibraryEffect(
                                new CardSubtypePredicate(CardSubtype.GATE),
                                LibrarySearchDestination.BATTLEFIELD),
                        new ConditionalEffect(
                                new ControlsDistinctPermanentNamesCount(
                                        10, new PermanentHasSubtypePredicate(CardSubtype.GATE)),
                                new WinGameEffect())
                ),
                "{3}, {T}, Return Maze's End to its owner's hand: Search your library for a Gate card, put it onto the battlefield, then shuffle. If you control ten or more Gates with different names, you win the game."
        ));
    }
}
