package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "159")
public class ScrapWelder extends Card {

    public ScrapWelder() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsArtifactPredicate(), "an artifact", false, false, true, false),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.ARTIFACT))
                                .targetGraveyard(true)
                                .grantHaste(true)
                                .requiresManaValueAtMostX(true)
                                .manaValueXOffset(-1)
                                .build()),
                "{T}, Sacrifice an artifact with mana value X: Return target artifact card with mana value less than X "
                        + "from your graveyard to the battlefield. It gains haste until end of turn."
        ));
    }
}
