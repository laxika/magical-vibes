package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "252")
public class GravestoneStrider extends Card {

    public GravestoneStrider() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}: Add one mana of any color. Activate only once each turn.",
                1
        ));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false)),
                "{2}, Exile this card from your graveyard: Exile target card from a graveyard."
        ));
    }
}
