package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;

@CardRegistration(set = "WTH", collectorNumber = "32")
public class AncestralKnowledge extends Card {

    public AncestralKnowledge() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));

        // No player target: the look falls back to the controller's own library.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LookAtTopCardsOfTargetLibraryEffect(10, TargetLibraryAction.MAY_EXILE_ANY_NUMBER));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new ShuffleLibraryEffect(false));
    }
}
