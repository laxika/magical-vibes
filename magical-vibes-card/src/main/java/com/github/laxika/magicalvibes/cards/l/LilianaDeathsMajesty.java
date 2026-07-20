package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "97")
public class LilianaDeathsMajesty extends Card {

    public LilianaDeathsMajesty() {
        // +1: Create a 2/2 black Zombie creature token. Mill two cards.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new CreateTokenEffect(1, "Zombie", 2, 2,
                                CardColor.BLACK, List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()),
                        new MillEffect(2, MillRecipient.CONTROLLER)
                ),
                "+1: Create a 2/2 black Zombie creature token. Mill two cards."
        ));

        // −3: Return target creature card from your graveyard to the battlefield.
        //     That creature is a black Zombie in addition to its other colors and types.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .grantColor(CardColor.BLACK)
                        .grantSubtype(CardSubtype.ZOMBIE)
                        .build()),
                "−3: Return target creature card from your graveyard to the battlefield. That creature is a black Zombie in addition to its other colors and types."
        ));

        // −7: Destroy all non-Zombie creatures.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))
                )))),
                "−7: Destroy all non-Zombie creatures."
        ));
    }
}
