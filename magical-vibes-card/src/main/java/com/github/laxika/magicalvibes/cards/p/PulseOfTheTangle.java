package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "80")
public class PulseOfTheTangle extends Card {

    public PulseOfTheTangle() {
        // Create a 3/3 green Beast creature token.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Beast", 3, 3, CardColor.GREEN, List.of(CardSubtype.BEAST), Set.of(), Set.of()));

        // Then if an opponent controls more creatures than you, return Pulse of the Tangle to its owner's hand.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new OpponentControlsMoreCreatures(1), ReturnToHandEffect.selfSpell()));
    }
}
