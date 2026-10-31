package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "XLN", collectorNumber = "95")
public class ContractKilling extends Card {

    public ContractKilling() {
        // Destroy target creature. Create two Treasure tokens.
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(CreateTokenEffect.ofTreasureToken(2));
    }
}
