package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
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
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(new DiscardEffect(1, Subjects.you()),
                "Discard a card? If you don't, you may pay 3 life to keep this scheme.",
                new MayPayLifeEffect(3, null, "Pay 3 life? If you don't, abandon this scheme.",
                        MayPayPayer.CONTROLLER, new SacrificeSelfEffect())));
    }
}
