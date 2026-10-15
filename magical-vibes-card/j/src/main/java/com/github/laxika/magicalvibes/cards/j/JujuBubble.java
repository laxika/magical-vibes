package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "147")
public class JujuBubble extends Card {

    public JujuBubble() {
        // Cumulative upkeep {1}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));

        // When you play a card, sacrifice this artifact.
        // "Play a card" = cast a spell or play a land (not put onto the battlefield by an effect).
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new SacrificeSelfEffect())
        ));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND, new SacrificeSelfEffect());

        // {2}: You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new GainLifeEffect(1)),
                "{2}: You gain 1 life."
        ));
    }
}
