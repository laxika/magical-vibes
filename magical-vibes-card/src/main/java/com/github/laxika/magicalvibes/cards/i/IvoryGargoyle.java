package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "5")
public class IvoryGargoyle extends Card {

    public IvoryGargoyle() {
        // Flying is auto-loaded from Scryfall keywords.
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new RegisterDelayedSelfReturnFromGraveyardEffect(),
                new SkipNextEffect(SkipKind.DRAW_STEP)));

        addActivatedAbility(new ActivatedAbility(false, "{4}{W}",
                List.of(new ExileSelfEffect()),
                "{4}{W}: Exile this creature."));
    }
}
