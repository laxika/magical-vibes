package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToSecondaryTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "128")
public class GoblinBarrage extends Card {

    public GoblinBarrage() {
        // Kicker—Sacrifice an artifact or Goblin.
        addEffect(EffectSlot.STATIC, new KickerEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)
                )),
                "an artifact or Goblin"
        ));

        // Goblin Barrage deals 4 damage to target creature.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));

        // If this spell was kicked, it also deals 4 damage to target player or planeswalker.
        // TODO: We should find a way to get rid of DealDamageToSecondaryTargetEffect and use DealDamageToTargetPlayerEffect instead
        addEffect(EffectSlot.SPELL, new KickedConditionalEffect(new DealDamageToSecondaryTargetEffect(4)));
    }
}
