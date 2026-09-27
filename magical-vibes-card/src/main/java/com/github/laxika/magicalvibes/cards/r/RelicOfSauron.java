package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "46")
@CardRegistration(set = "HOC", collectorNumber = "86")
@CardRegistration(set = "LTC", collectorNumber = "79")
@CardRegistration(set = "LTC", collectorNumber = "159")
public class RelicOfSauron extends Card {

    public RelicOfSauron() {
        // {T}: Add two mana in any combination of {U}, {B}, and/or {R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(
                        List.of(ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED), 2)),
                "{T}: Add two mana in any combination of {U}, {B}, and/or {R}."
        ));

        // {3}, {T}: Draw two cards, then discard a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new DrawCardEffect(2), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{3}, {T}: Draw two cards, then discard a card."
        ));
    }
}
