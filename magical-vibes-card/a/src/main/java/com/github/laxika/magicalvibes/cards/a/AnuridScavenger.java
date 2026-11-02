package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TOR", collectorNumber = "119")
public class AnuridScavenger extends Card {

    public AnuridScavenger() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ForcedCostOrElseEffect(
                new PutCardFromGraveyardOnBottomOfLibraryCost(),
                List.of(new SacrificeSelfEffect()),
                true));
    }
}
