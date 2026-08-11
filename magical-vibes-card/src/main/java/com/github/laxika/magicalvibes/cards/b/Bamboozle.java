package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ODY", collectorNumber = "68")
public class Bamboozle extends Card {

    public Bamboozle() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ))
                .addEffect(EffectSlot.SPELL,
                        new LookAtTopCardsOfTargetLibraryEffect(4,
                                TargetLibraryAction.REVEAL_AND_PUT_ONE_INTO_GRAVEYARD))
                .addEffect(EffectSlot.SPELL,
                        new LookAtTopCardsOfTargetLibraryEffect(3,
                                TargetLibraryAction.REVEAL_AND_PUT_ONE_INTO_GRAVEYARD));
    }
}
