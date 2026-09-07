package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

import java.util.List;

@CardRegistration(set = "TSB", collectorNumber = "54")
public class WitheredWretch extends Card {

    public WitheredWretch() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD)),
                "{1}: Exile target card from a graveyard."
        ));
    }
}
