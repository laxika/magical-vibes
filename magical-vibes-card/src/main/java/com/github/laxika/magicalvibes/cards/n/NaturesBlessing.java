package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "110")
public class NaturesBlessing extends Card {

    public NaturesBlessing() {
        // {G}{W}, Discard a card: Put a +1/+1 counter on target creature or that creature gains
        // banding, first strike, or trample. The keyword grants state no duration, so they last
        // until the end of the game (CR 611.2a).
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{W}",
                List.of(new DiscardCardTypeCost(null, null),
                        new ChooseOneForTargetCreatureEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption("Put a +1/+1 counter on it",
                                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                                new ChooseOneEffect.ChooseOneOption("It gains banding",
                                        grant(Keyword.BANDING)),
                                new ChooseOneEffect.ChooseOneOption("It gains first strike",
                                        grant(Keyword.FIRST_STRIKE)),
                                new ChooseOneEffect.ChooseOneOption("It gains trample",
                                        grant(Keyword.TRAMPLE))))),
                "{G}{W}, Discard a card: Put a +1/+1 counter on target creature or that creature "
                        + "gains banding, first strike, or trample."));
    }

    private static GrantKeywordEffect grant(Keyword keyword) {
        return new GrantKeywordEffect(keyword, GrantScope.TARGET, GrantDuration.INDEFINITE);
    }
}
