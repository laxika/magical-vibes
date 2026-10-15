package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "112")
public class CatacombDragon extends Card {

    public CatacombDragon() {
        // Whenever this creature becomes blocked by a nonartifact, non-Dragon creature, that creature
        // gets -X/-0 until end of turn, where X is half the creature's power, rounded down. One trigger
        // per qualifying blocker; the blocker is carried as the trigger's non-targeting target, and
        // TargetPower reads its power at resolution.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsArtifactPredicate()),
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)))),
                        new BoostTargetCreatureEffect(
                                new Scaled(new Divided(new TargetPower(), 2), -1),
                                new Fixed(0))),
                TriggerMode.PER_BLOCKER);
    }
}
