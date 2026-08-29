package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ROE", collectorNumber = "169")
public class TuktukTheExplorer extends Card {

    public TuktukTheExplorer() {
        // When Tuktuk the Explorer dies, create Tuktuk the Returned, a legendary 5/5
        // colorless Goblin Golem artifact creature token.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                CardType.CREATURE, 1, "Tuktuk the Returned", 5, 5, null, null,
                List.of(CardSubtype.GOBLIN, CardSubtype.GOLEM), Set.of(),
                Set.of(CardType.ARTIFACT), false, false, Map.of(), List.of(),
                false, false, true, 0, Set.of()));
    }
}
