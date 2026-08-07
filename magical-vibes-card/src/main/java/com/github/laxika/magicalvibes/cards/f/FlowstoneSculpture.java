package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfKeywordIndefinitelyEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "288")
public class FlowstoneSculpture extends Card {

    public FlowstoneSculpture() {
        // {2}, Discard a card: Put a +1/+1 counter on this creature or this creature gains flying,
        // first strike, or trample. (This effect lasts indefinitely.) The keyword modes have no
        // stated duration (CR 611.2b), so they use SetSelfKeywordIndefinitelyEffect rather than the
        // until-end-of-turn GrantKeywordEffect. No mode targets, so the mode is picked on resolution.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new DiscardCardTypeCost(null, null), new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Put a +1/+1 counter on this creature.",
                                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))),
                        new ChooseOneEffect.ChooseOneOption("This creature gains flying.",
                                List.of(new SetSelfKeywordIndefinitelyEffect(Keyword.FLYING, true))),
                        new ChooseOneEffect.ChooseOneOption("This creature gains first strike.",
                                List.of(new SetSelfKeywordIndefinitelyEffect(Keyword.FIRST_STRIKE, true))),
                        new ChooseOneEffect.ChooseOneOption("This creature gains trample.",
                                List.of(new SetSelfKeywordIndefinitelyEffect(Keyword.TRAMPLE, true)))))),
                "{2}, Discard a card: Put a +1/+1 counter on this creature or this creature gains flying, first strike, or trample."
        ));
    }
}
