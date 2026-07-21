package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "71")
public class MercilessEternal extends Card {

    public MercilessEternal() {
        // Afflict 2 — whenever this creature becomes blocked, the defending player loses 2 life
        // (once per becoming blocked). Heads-up, so the sole opponent is the defender.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
        // {2}{B}, Discard a card: This creature gets +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostSelfEffect(2, 2)
                ),
                "{2}{B}, Discard a card: This creature gets +2/+2 until end of turn."
        ));
    }
}
