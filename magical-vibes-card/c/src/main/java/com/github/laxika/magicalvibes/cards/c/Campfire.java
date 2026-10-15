package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCommandersIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "374")
public class Campfire extends Card {

    public Campfire() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new GainLifeEffect(2)),
                "{1}, {T}: You gain 2 life."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new ExileSelfCost(),
                        new PutAllCommandersIntoHandEffect(),
                        new ShuffleGraveyardIntoLibraryEffect(false)
                ),
                "{2}, {T}, Exile Campfire: Put all commanders you own from the command zone and from your graveyard into your hand. Then shuffle your graveyard into your library."
        ));
    }
}
