package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "6")
public class ApostleOfPurifyingLight extends Card {

    public ApostleOfPurifyingLight() {
        // {2}: Exile target card from a graveyard.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD)),
                "{2}: Exile target card from a graveyard."
        ));
    }
}
