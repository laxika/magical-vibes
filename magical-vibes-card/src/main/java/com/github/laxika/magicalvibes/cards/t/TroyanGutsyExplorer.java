package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "217")
public class TroyanGutsyExplorer extends Card {

    public TroyanGutsyExplorer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardRestrictedManaEffect(
                                ManaColor.GREEN, 1, new ManaRestriction.ManaValueAtLeastFiveOrXCosts()),
                        new AwardRestrictedManaEffect(
                                ManaColor.BLUE, 1, new ManaRestriction.ManaValueAtLeastFiveOrXCosts())
                ),
                "{T}: Add {G}{U}. Spend this mana only to cast spells with mana value 5 or greater or spells with {X} in their mana costs."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{U}, {T}: Draw a card, then discard a card."
        ));
    }
}
