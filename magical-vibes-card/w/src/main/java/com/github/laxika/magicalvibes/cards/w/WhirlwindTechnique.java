package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLE", collectorNumber = "100")
public class WhirlwindTechnique extends Card {

    public WhirlwindTechnique() {
        PlayerPredicateTargetFilter anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");

        target(anyPlayer)
                .addEffect(EffectSlot.SPELL, new DrawCardForTargetPlayerEffect(2, false, true))
                .addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, new AirbendTargetPermanentEffect());
    }
}
