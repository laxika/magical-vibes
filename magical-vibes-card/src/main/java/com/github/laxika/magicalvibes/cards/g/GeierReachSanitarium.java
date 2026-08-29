package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "203")
public class GeierReachSanitarium extends Card {

    public GeierReachSanitarium() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {2}, {T}: Each player draws a card, then discards a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new EachPlayerDrawsCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.EACH_PLAYER)
                ),
                "{2}, {T}: Each player draws a card, then discards a card."
        ));
    }
}
