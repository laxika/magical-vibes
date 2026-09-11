package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "83")
public class TheSackvilleBagginses extends Card {

    private static final PermanentAllOfPredicate ANOTHER_CREATURE_OR_ARTIFACT =
            new PermanentAllOfPredicate(List.of(
                    new PermanentAnyOfPredicate(List.of(
                            new PermanentIsCreaturePredicate(),
                            new PermanentIsArtifactPredicate())),
                    new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    private static final PlayerPredicateTargetFilter OPPONENT_TARGET = new PlayerPredicateTargetFilter(
            new PlayerRelationPredicate(PlayerRelation.OPPONENT),
            "Target must be an opponent");

    public TheSackvilleBagginses() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SacrificePermanentThenEffect(
                        ANOTHER_CREATURE_OR_ARTIFACT,
                        SequenceEffect.of(new DrawCardEffect(), CreateTokenEffect.ofTreasureToken(1)),
                        "another creature or artifact"),
                "Sacrifice another creature or artifact?"));

        target(OPPONENT_TARGET).addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsTokenPredicate(),
                        new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)));
    }
}
