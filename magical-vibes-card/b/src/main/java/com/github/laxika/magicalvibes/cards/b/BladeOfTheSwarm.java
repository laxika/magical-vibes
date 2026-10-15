package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetExiledCardOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "90")
public class BladeOfTheSwarm extends Card {

    public BladeOfTheSwarm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Put two +1/+1 counters on this creature.",
                                new PutCountersOnSourceEffect(1, 1, 2)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Put target exiled card with warp on the bottom of its owner's library.",
                                new PutTargetExiledCardOnBottomOfOwnersLibraryEffect(
                                        new CardKeywordPredicate(Keyword.WARP)))))));
    }
}
