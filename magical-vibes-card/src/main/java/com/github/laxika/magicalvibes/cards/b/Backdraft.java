package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DamageDealtByTargetPlayerSorceryThisTurn;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerCastSorceryThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

@CardRegistration(set = "LEG", collectorNumber = "132")
public class Backdraft extends Card {

    public Backdraft() {
        target(new PlayerPredicateTargetFilter(
                new PlayerCastSorceryThisTurnPredicate(),
                "Target player must have cast a sorcery this turn."
        )).addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(
                new Divided(new DamageDealtByTargetPlayerSorceryThisTurn(), 2),
                DamageRecipient.TARGET_PLAYER));
    }
}
