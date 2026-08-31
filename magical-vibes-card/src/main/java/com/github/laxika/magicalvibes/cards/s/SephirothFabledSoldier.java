package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "115")
@CardRegistration(set = "FIN", collectorNumber = "451")
@CardRegistration(set = "FIN", collectorNumber = "317")
@CardRegistration(set = "FIN", collectorNumber = "382")
@CardRegistration(set = "FIN", collectorNumber = "527")
public class SephirothFabledSoldier extends Card {

    static final PermanentAllOfPredicate ANOTHER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    static final PlayerPredicateTargetFilter OPPONENT_TARGET = new PlayerPredicateTargetFilter(
            new PlayerRelationPredicate(PlayerRelation.OPPONENT),
            "Target must be an opponent");

    static final SequenceEffect DRAIN = SequenceEffect.of(
            new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
            new GainLifeEffect(1));

    private static final MayEffect SACRIFICE_ANOTHER_AND_DRAW = new MayEffect(
            new SacrificePermanentThenEffect(ANOTHER_CREATURE, new DrawCardEffect(1),
                    "another creature"),
            "Sacrifice another creature?");

    public SephirothFabledSoldier() {
        setBackFaceCard(new SephirothOneWingedAngel());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SACRIFICE_ANOTHER_AND_DRAW);
        addEffect(EffectSlot.ON_ATTACK, SACRIFICE_ANOTHER_AND_DRAW);

        target(OPPONENT_TARGET).addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                SequenceEffect.of(
                        DRAIN,
                        new ConditionalEffect(
                                new NthAbilityResolutionThisTurn(4),
                                new TransformSelfEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "SephirothOneWingedAngel";
    }
}
