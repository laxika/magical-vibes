package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "130")
@CardRegistration(set = "TLE", collectorNumber = "201")
public class AvatarKyoshiEarthbender extends Card {

    public AvatarKyoshiEarthbender() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));

        target(TargetFilters.landYouControl())
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new EarthbendTargetLandEffect(8))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
