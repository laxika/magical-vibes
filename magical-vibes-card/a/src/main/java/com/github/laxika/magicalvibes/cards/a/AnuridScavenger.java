package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "TOR", collectorNumber = "119")
public class AnuridScavenger extends Card {

    public AnuridScavenger() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.unlessYouPay(
                new PutCardsFromGraveyardOnBottomOfLibraryCost(1),
                new SacrificeSelfEffect()));
    }
}
