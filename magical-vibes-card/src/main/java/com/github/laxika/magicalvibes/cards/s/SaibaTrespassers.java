package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "77")
public class SaibaTrespassers extends Card {

    public SaibaTrespassers() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new SkipNextUntapEffect(TapUntapScope.TARGET)),
                "Channel — {3}{U}, Discard this card: Tap up to two target creatures you don't control. "
                        + "Those creatures don't untap during their controller's next untap step.",
                List.<TargetFilter>of(
                        TargetFilters.creatureAnOpponentControls(),
                        TargetFilters.creatureAnOpponentControls()),
                0,
                2));
    }
}
