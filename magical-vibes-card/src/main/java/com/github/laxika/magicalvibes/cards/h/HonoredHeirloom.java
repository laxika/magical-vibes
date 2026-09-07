package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "257")
public class HonoredHeirloom extends Card {

    public HonoredHeirloom() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false)),
                "{2}, {T}: Exile target card from a graveyard."
        ));
    }
}
