package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "BIG", collectorNumber = "29")
public class FomoriVault extends Card {

    public FomoriVault() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(
                                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER), 1)
                ),
                "{3}, {T}, Discard a card: Look at the top X cards of your library, where X is the number of artifacts you control. "
                        + "Put one of those cards into your hand and the rest on the bottom of your library in a random order."
        ));
    }
}
