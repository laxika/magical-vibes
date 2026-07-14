package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "131")
public class WormHarvest extends Card {

    public WormHarvest() {
        // Create a 1/1 black and green Worm creature token for each land card in your graveyard.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                new CardsInGraveyard(new CardTypePredicate(CardType.LAND), CountScope.CONTROLLER),
                "Worm", 1, 1, CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN), List.of(CardSubtype.WORM),
                Set.of(), Set.of(),
                false, false,
                Map.of(), List.of(), false, false, false, 0, Set.of()
        ));

        // Retrace (You may cast this card from your graveyard by discarding a land card in
        // addition to paying its other costs.)
        addCastingOption(new Retrace());
    }
}
