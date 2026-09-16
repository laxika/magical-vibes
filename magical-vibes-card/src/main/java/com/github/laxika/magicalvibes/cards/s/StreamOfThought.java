package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "71")
public class StreamOfThought extends Card {

    public StreamOfThought() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{2}{U}{U}")));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL, new MillEffect(4, MillRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL,
                new ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(null, 4));
        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{2}{U}{U}"));
    }
}
