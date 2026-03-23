package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "49")
public class DaringSaboteur extends Card {

    public DaringSaboteur() {
        // {2}{U}: Daring Saboteur can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{2}{U}: Daring Saboteur can't be blocked this turn."
        ));

        // Whenever Daring Saboteur deals combat damage to a player, you may draw a card.
        // If you do, discard a card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                new DrawAndDiscardCardEffect(), "Draw a card and discard a card?"
        ));
    }
}
