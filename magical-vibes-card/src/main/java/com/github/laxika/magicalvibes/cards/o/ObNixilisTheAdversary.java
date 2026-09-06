package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfCasualtyPaidEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayDiscardOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ChooseXValueCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "206")
public class ObNixilisTheAdversary extends Card {

    public ObNixilisTheAdversary() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfCasualtyPaidEffect(
                Set.of(CardSupertype.LEGENDARY), true, true));
        addEffect(EffectSlot.SPELL, CasualtyCost.matchingChosenX());
        addEffect(EffectSlot.SPELL, new ChooseXValueCost(0, Integer.MAX_VALUE));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new EachOpponentMayDiscardOrLoseLifeEffect(2),
                        new ConditionalEffect(
                                new ControlsPermanent(new PermanentAnyOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.DEMON),
                                        new PermanentHasSubtypePredicate(CardSubtype.DEVIL)))),
                                new GainLifeEffect(2))
                ),
                "+1: Each opponent loses 2 life unless they discard a card. If you control a Demon or Devil, you gain 2 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        1, "Devil", 1, 1, CardColor.RED, List.of(CardSubtype.DEVIL),
                        Set.of(), Set.of(), Map.of(
                                EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1)))),
                "−2: Create a 1/1 red Devil creature token with \"When this token dies, it deals 1 damage to any target.\""
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(
                        new DrawCardForTargetPlayerEffect(7, false, true),
                        new LoseLifeEffect(7, LoseLifeRecipient.TARGET_PLAYER)
                ),
                "−7: Target player draws seven cards and loses 7 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"))
        );
    }
}
