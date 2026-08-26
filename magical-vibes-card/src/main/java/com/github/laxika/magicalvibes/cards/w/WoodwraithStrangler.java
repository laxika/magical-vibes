package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "241")
public class WoodwraithStrangler extends Card {

    public WoodwraithStrangler() {
        // Exile a creature card from your graveyard: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileCardFromGraveyardCost(CardType.CREATURE), new RegenerateEffect()),
                "Exile a creature card from your graveyard: Regenerate this creature."
        ));
    }
}
