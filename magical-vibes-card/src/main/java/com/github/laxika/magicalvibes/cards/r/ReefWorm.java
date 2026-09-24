package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "A25", collectorNumber = "70")
@CardRegistration(set = "C14", collectorNumber = "16")
public class ReefWorm extends Card {

    public ReefWorm() {
        // When Reef Worm dies, create a 3/3 blue Fish token.
        // It has "When this token dies, create a 6/6 blue Whale token."
        // That token has "When this token dies, create a 9/9 blue Kraken token."
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Fish", 3, 3, CardColor.BLUE, List.of(CardSubtype.FISH), Set.of(), Set.of(),
                Map.of(EffectSlot.ON_DEATH, new CreateTokenEffect(
                        1, "Whale", 6, 6, CardColor.BLUE, List.of(CardSubtype.WHALE), Set.of(), Set.of(),
                        Map.of(EffectSlot.ON_DEATH, new CreateTokenEffect(
                                "Kraken", 9, 9, CardColor.BLUE, List.of(CardSubtype.KRAKEN), Set.of(), Set.of()
                        ))
                ))
        ));
    }
}
