package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "93")
public class FrontlineDevastator extends Card {

    public FrontlineDevastator() {
        // Afflict 2 — whenever this creature becomes blocked, the defending player loses 2 life
        // (once per becoming blocked, not per blocker). Heads-up, so the sole opponent is the defender.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));

        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new BoostSelfEffect(1, 0)),
                "{1}{R}: This creature gets +1/+0 until end of turn."));
    }
}
