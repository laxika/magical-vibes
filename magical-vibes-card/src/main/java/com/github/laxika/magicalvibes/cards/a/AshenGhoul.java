package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.CardsAboveSelfInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "114")
public class AshenGhoul extends Card {

    public AshenGhoul() {
        // Haste (keyword from Scryfall)

        // {B}: Return this card from your graveyard to the battlefield. Activate only during your
        // upkeep and only if three or more creature cards are above this card.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{B}: Return this card from your graveyard to the battlefield. Activate only during "
                        + "your upkeep and only if three or more creature cards are above this card.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withActivationCondition(
                new CardsAboveSelfInGraveyard(3, new CardTypePredicate(CardType.CREATURE)),
                "Activate only if three or more creature cards are above this card"));
    }
}
