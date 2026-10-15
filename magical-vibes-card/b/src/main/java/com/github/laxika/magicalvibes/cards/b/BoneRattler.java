package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "35")
public class BoneRattler extends Card {

    public BoneRattler() {
        CreateTokenEffect skeletonToken = new CreateTokenEffect(
                "Reassembling Skeleton",
                1,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.SKELETON, CardSubtype.WARRIOR),
                Set.of(),
                Set.of());
        ActivatedAbility returnAbility = new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .enterTapped(true)
                        .build()),
                "{1}{B}: Return Reassembling Skeleton from your graveyard to the battlefield tapped.");
        CreateTokenCardsInGraveyardEffect tokenCards = new CreateTokenCardsInGraveyardEffect(
                4,
                skeletonToken,
                "{1}{B}",
                "{1}{B}: Return Reassembling Skeleton from your graveyard to the battlefield tapped.",
                List.of(returnAbility));

        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new ExileSourceCardFromGraveyardThenEffect(tokenCards));
    }
}
