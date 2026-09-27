package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsController;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2448")
public class TheGhoulGunslinger extends Card {

    private static final CardEffect RAD_AND_TREASURE = SequenceEffect.of(
            new GiveTargetPlayerRadCountersEffect(2),
            new ConditionalEffect(new TargetPlayerIsController(),
                    CreateTokenEffect.ofTreasureToken(1)));

    private static final CardEffect ZOMBIE_OR_MUTANT_RAD_AND_TREASURE = new TriggeringCardConditionalEffect(
            new CardAnyOfPredicate(List.of(
                    new CardSubtypePredicate(CardSubtype.ZOMBIE),
                    new CardSubtypePredicate(CardSubtype.MUTANT))),
            RAD_AND_TREASURE);

    public TheGhoulGunslinger() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"));
        addEffect(EffectSlot.ON_DEATH, RAD_AND_TREASURE);
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, ZOMBIE_OR_MUTANT_RAD_AND_TREASURE);
    }
}
