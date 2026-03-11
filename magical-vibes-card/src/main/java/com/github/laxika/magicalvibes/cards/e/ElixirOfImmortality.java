package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfAndGraveyardIntoLibraryEffect;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "206")
public class ElixirOfImmortality extends Card {

    public ElixirOfImmortality() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new GainLifeEffect(5), new ShuffleSelfAndGraveyardIntoLibraryEffect()),
                "{2}, {T}: You gain 5 life. Shuffle Elixir of Immortality and your graveyard into their owner's library."
        ));
    }
}
