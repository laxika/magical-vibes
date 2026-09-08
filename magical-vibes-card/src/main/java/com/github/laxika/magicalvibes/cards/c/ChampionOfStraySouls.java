package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeXPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "63")
public class ChampionOfStraySouls extends Card {

    public ChampionOfStraySouls() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{B}{B}",
                List.of(
                        new SacrificeXPermanentsCost(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                        ))),
                        new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                                new CardTypePredicate(CardType.CREATURE))
                ),
                "{3}{B}{B}, {T}, Sacrifice X other creatures: Return X target creature cards from your graveyard to the battlefield.",
                null,
                null,
                null,
                null,
                List.of(),
                0,
                100
        ).withXScaledTargets());

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{5}{B}{B}: Put this card from your graveyard on top of your library."
        ));
    }
}
