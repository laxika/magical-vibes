package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "27")
public class MangaraTheDiplomat extends Card {

    public MangaraTheDiplomat() {
        // Whenever an opponent attacks with two or more creatures at you and/or your planeswalkers, draw a card.
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(new OpponentAttacksWithAtLeastCreatures(2), new DrawCardEffect()));

        // Whenever an opponent casts their second spell each turn, draw a card.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new NthSpellCastTriggerEffect(2, List.of(new DrawCardEffect())));
    }
}
