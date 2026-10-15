package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "286")
public class TezzeretCruelMachinist extends Card {

    public TezzeretCruelMachinist() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect()),
                "+1: Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new AnimatePermanentsEffect(5, 5, List.of(), Set.of(), null,
                        Set.of(CardType.CREATURE), GrantScope.TARGET, EffectDuration.UNTIL_YOUR_NEXT_TURN)),
                "0: Until your next turn, target artifact you control becomes a 5/5 creature in addition to its other types.",
                new ControlledPermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(),
                        "Target must be an artifact you control")
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(PutCardToBattlefieldEffect.anyNumberFaceDown(5, 5,
                        Set.of(CardType.ARTIFACT, CardType.CREATURE))),
                "\u22127: Put any number of cards from your hand onto the battlefield face down. They're 5/5 artifact creatures."
        ));
    }
}
