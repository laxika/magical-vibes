package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "81")
public class ChandraFlameshaper extends Card {

    public ChandraFlameshaper() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(
                        new AwardManaEffect(ManaColor.RED, 3),
                        new ExileTopCardsChooseOneMayPlayThisTurnEffect(3)),
                "+2: Add {R}{R}{R}. Exile the top three cards of your library. Choose one. You may play that card this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenCopyOfTargetPermanentEffect(true, false, true)),
                "+1: Create a token that's a copy of target creature you control, except it has haste and \"At the beginning of the end step, sacrifice this token.\"",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate())),
                        "Target must be a creature you control.")));

        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(DealDividedDamageEffect.chosenAmongTargetCreaturesAndPlaneswalkers(8)),
                "-4: Chandra deals 8 damage divided as you choose among any number of target creatures and/or planeswalkers."
        ));
    }
}
