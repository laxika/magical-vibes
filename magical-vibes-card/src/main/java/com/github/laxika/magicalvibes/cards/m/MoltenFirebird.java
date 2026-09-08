package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "117")
public class MoltenFirebird extends Card {

    public MoltenFirebird() {
        addEffect(EffectSlot.ON_DEATH, new RegisterDelayedSelfReturnFromGraveyardEffect());
        addEffect(EffectSlot.ON_DEATH, new SkipNextEffect(SkipKind.DRAW_STEP));

        addActivatedAbility(new ActivatedAbility(false, "{4}{R}",
                List.of(new ExileSelfEffect()),
                "{4}{R}: Exile this creature."));
    }
}
