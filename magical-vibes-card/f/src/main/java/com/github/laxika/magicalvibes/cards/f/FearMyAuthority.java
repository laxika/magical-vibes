package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "333")
public class FearMyAuthority extends Card {

    public FearMyAuthority() {
        addStaticEffect(new StaticBoostEffect(2, 2, Set.of(Keyword.FEAR), Subjects.creaturesYouControl()));
        // At the beginning of your upkeep, abandon this scheme unless you discard a card or pay 3 life. A scheme's
        // "sacrifice" of itself is its abandon (CR 701.33b).
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new UnlessPaysEffect(Subjects.you(),
                AnyOfCost.of(new DiscardCardTypeCost(null, null), new PayLifeCost(3)), new SacrificeSelfEffect(), null));
    }
}
