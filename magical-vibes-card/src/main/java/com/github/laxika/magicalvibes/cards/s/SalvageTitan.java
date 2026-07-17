package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "84")
public class SalvageTitan extends Card {

    public SalvageTitan() {
        // You may sacrifice three artifacts rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                new SacrificePermanentsCost(3, new PermanentIsArtifactPredicate()))));

        // Exile three artifact cards from your graveyard: Return this card from your graveyard to your hand.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileNCardsFromGraveyardCost(3, CardType.ARTIFACT),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()),
                "Exile three artifact cards from your graveyard: Return Salvage Titan from your graveyard to your hand."));
    }
}
