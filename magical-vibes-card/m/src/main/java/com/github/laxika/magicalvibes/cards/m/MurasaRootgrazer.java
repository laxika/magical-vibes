package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "229")
public class MurasaRootgrazer extends Card {

    public MurasaRootgrazer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(CardPredicateUtils.basicLand(), "basic land"),
                        "Put a basic land card from your hand onto the battlefield?"
                )),
                "{T}: You may put a basic land card from your hand onto the battlefield."
        ));

        PermanentPredicate basicLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.BASIC)));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(ReturnToHandEffect.target(basicLand)),
                "{T}: Return target basic land you control to its owner's hand.",
                new ControlledPermanentPredicateTargetFilter(basicLand, "Target must be a basic land you control")
        ));
    }
}
