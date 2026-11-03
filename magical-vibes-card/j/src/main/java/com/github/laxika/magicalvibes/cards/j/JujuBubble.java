package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "147")
public class JujuBubble extends Card {

    public JujuBubble() {
        // Cumulative upkeep {1}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{1}"));

        // When you play a card, sacrifice this artifact.
        // "Play a card" = cast a spell or play a land (not put onto the battlefield by an effect).
        addTrigger(Triggers.youCastSpell().anySpell(), new SacrificeSelfEffect());
        addTrigger(Triggers.youPlayLand(), new SacrificeSelfEffect());

        // {2}: You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new GainLifeEffect(1)),
                "{2}: You gain 1 life."
        ));
    }
}
