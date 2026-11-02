package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipCoinForTriggeringSpellAndCounterOnLossEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfTargetPlayerMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "OPCA", collectorNumber = "51")
public class MirroredDepths extends Card {

    public MirroredDepths() {
        addTrigger(Triggers.playerCastsSpell(),
                new SpellCastTriggerEffect(null,
                        List.of(new FlipCoinForTriggeringSpellAndCounterOnLossEffect())));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addTrigger(Triggers.chaosEnsues(),
                        new RevealTopCardOfTargetPlayerMayCastFreeEffect());
    }
}
