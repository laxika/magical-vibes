package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "142")
@CardRegistration(set = "WHO", collectorNumber = "425")
@CardRegistration(set = "WHO", collectorNumber = "747")
@CardRegistration(set = "WHO", collectorNumber = "1016")
public class MadameVastra extends Card {

    private static final String PARTNER_NAME = "Jenny Flint";

    public MadameVastra() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put Jenny Flint into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));

        addStaticEffect(new MustBeBlockedIfAbleEffect());
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), SequenceEffect.of(
                CreateTokenEffect.ofClueToken(1),
                CreateTokenEffect.ofFoodToken(1)));
    }
}
