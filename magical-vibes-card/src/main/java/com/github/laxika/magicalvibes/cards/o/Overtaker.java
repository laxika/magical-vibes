package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "89")
public class Overtaker extends Card {

    public Overtaker() {
        // {3}{U}, {T}, Discard a card: Untap target creature and gain control of it until end of
        // turn. That creature gains haste until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                "{3}{U}, {T}, Discard a card: Untap target creature and gain control of it until end of turn. That creature gains haste until end of turn.",
                TargetFilters.creature()));
    }
}
