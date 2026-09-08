package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "101")
public class HellBentRaider extends Card {

    public HellBentRaider() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardRandomCardCost(),
                        new GrantProtectionFromColorUntilEndOfTurnEffect(CardColor.WHITE, GrantScope.SELF)
                ),
                "Discard a card at random: This creature gains protection from white until end of turn."
        ));
    }
}
