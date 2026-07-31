package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextDrawStepEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "5")
public class IvoryGargoyle extends Card {

    public IvoryGargoyle() {
        // Flying is auto-loaded from Scryfall keywords.
        addEffect(EffectSlot.ON_DEATH, new RegisterDelayedSelfReturnFromGraveyardEffect());
        addEffect(EffectSlot.ON_DEATH, new SkipNextDrawStepEffect());

        addActivatedAbility(new ActivatedAbility(false, "{4}{W}",
                List.of(new ExileSelfEffect()),
                "{4}{W}: Exile this creature."));
    }
}
