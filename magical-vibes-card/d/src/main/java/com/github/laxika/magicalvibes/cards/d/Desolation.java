package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "58")
public class Desolation extends Card {

    public Desolation() {
        // At the beginning of each end step, each player who tapped a land for mana this turn
        // sacrifices a land of their choice. This enchantment deals 2 damage to each player who
        // sacrificed a Plains this way.
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                new PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect(CardSubtype.PLAINS, 2));
    }
}
