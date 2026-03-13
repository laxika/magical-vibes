package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapPermanentsOfTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsOfTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "M11", collectorNumber = "73")
public class Sleep extends Card {

    public Sleep() {
        // Tap all creatures target player controls.
        // Those creatures don't untap during that player's next untap step.
        setTargetFilter(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ));
        addEffect(EffectSlot.SPELL, new TapPermanentsOfTargetPlayerEffect(new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new SkipNextUntapPermanentsOfTargetPlayerEffect(new PermanentIsCreaturePredicate()));
    }
}
