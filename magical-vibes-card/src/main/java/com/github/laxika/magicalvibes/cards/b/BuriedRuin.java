package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "224")
@CardRegistration(set = "2XM", collectorNumber = "312")
@CardRegistration(set = "C14", collectorNumber = "286")
@CardRegistration(set = "PIP", collectorNumber = "254")
@CardRegistration(set = "PIP", collectorNumber = "782")
@CardRegistration(set = "BRC", collectorNumber = "177")
@CardRegistration(set = "ONC", collectorNumber = "147")
@CardRegistration(set = "C18", collectorNumber = "239")
@CardRegistration(set = "EOC", collectorNumber = "150")
@CardRegistration(set = "C16", collectorNumber = "284")
@CardRegistration(set = "FDC", collectorNumber = "298")
@CardRegistration(set = "CM2", collectorNumber = "241")
public class BuriedRuin extends Card {

    public BuriedRuin() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {2}, {T}, Sacrifice Buried Ruin: Return target artifact card from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.ARTIFACT))
                                .targetGraveyard(true)
                                .build()
                ),
                "{2}, {T}, Sacrifice Buried Ruin: Return target artifact card from your graveyard to your hand."
        ));
    }
}
