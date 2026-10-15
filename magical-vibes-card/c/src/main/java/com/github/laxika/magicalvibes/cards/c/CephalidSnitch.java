package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.RemoveProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "30")
public class CephalidSnitch extends Card {

    public CephalidSnitch() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(),
                        new RemoveProtectionFromColorUntilEndOfTurnEffect(CardColor.BLACK)),
                "Sacrifice this creature: Target creature loses protection from black until end of turn.",
                TargetFilters.creature()
        ));
    }
}
