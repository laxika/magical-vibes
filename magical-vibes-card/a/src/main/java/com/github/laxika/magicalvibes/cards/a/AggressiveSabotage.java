package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "DMU", collectorNumber = "78")
public class AggressiveSabotage extends Card {

    public AggressiveSabotage() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{R}"));

        var targetPlayer = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ));
        targetPlayer.addEffect(EffectSlot.SPELL,
                new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));
        targetPlayer.addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new Kicked(),
                        new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER)));
    }
}
