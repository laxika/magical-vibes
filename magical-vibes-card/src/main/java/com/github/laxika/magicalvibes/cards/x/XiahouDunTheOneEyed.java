package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "92")
public class XiahouDunTheOneEyed extends Card {

    public XiahouDunTheOneEyed() {
        // Horsemanship — auto-loaded from Scryfall.
        //
        // Sacrifice Xiahou Dun: Return target black card from your graveyard to your hand.
        // Activate only during your turn, before attackers are declared.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardColorPredicate(CardColor.BLACK))
                        .targetGraveyard(true)
                        .build()),
                "Sacrifice Xiahou Dun: Return target black card from your graveyard to your hand. "
                        + "Activate only during your turn, before attackers are declared.",
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED
        ));
    }
}
