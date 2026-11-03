package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "18")
public class PlanarCollapse extends Card {

    public PlanarCollapse() {
        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new AnyPlayerControlsPermanentCount(4, creature)),
                SequenceEffect.of(new SacrificeSelfEffect(), new DestroyAllPermanentsEffect(creature, true)));
    }
}
