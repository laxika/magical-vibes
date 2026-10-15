package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfManaValueEqualsXEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "66")
public class HisokaMinamoSensei extends Card {

    public HisokaMinamoSensei() {
        // {2}{U}, Discard a card: Counter target spell if it has the same mana value as the discarded card.
        addActivatedAbility(new ActivatedAbility(false, "{2}{U}",
                List.of(
                        new DiscardCardTypeCost(null, null, false, 1, false, true),
                        new CounterSpellIfManaValueEqualsXEffect()),
                "{2}{U}, Discard a card: Counter target spell if it has the same mana value as the discarded card."));
    }
}
