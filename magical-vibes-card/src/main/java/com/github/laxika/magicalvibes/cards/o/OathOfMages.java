package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerHasMoreLifeThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

@CardRegistration(set = "EXO", collectorNumber = "90")
public class OathOfMages extends Card {

    public OathOfMages() {
        // At the beginning of each player's upkeep, that player chooses target player who has more
        // life than they do and is their opponent. The first player may have this enchantment deal
        // 1 damage to the second player.
        target(new PlayerPredicateTargetFilter(
                new PlayerHasMoreLifeThanControllerPredicate(),
                "Target player must be an opponent who has more life than you"
        )).addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new MayEffect(
                new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER),
                "Have Oath of Mages deal 1 damage to that player?",
                null,
                MayChoicePlayer.ACTIVE_PLAYER));
    }
}
