package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlipCoinForTriggeringSpellAndCounterOnLossEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfTargetPlayerMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "OPCA", collectorNumber = "51")
public class MirroredDepths extends Card {

    public MirroredDepths() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(null,
                        List.of(new FlipCoinForTriggeringSpellAndCounterOnLossEffect())));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addEffect(EffectSlot.CHAOS_TRIGGERED,
                        new RevealTopCardOfTargetPlayerMayCastFreeEffect());
    }
}
