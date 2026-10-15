package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "AVR", collectorNumber = "121")
public class TreacherousPitDweller extends Card {

    public TreacherousPitDweller() {
        // Undying is auto-loaded as a keyword from Scryfall; its return mechanic is handled by the engine.
        // The trigger only fires when it enters from a graveyard (undying, reanimation), never on a
        // normal cast — so the opponent target is chosen as the ability goes on the stack, not at cast time.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_SELF_ENTERS_FROM_GRAVEYARD, new TargetPlayerGainsControlOfSourceCreatureEffect());
    }
}
