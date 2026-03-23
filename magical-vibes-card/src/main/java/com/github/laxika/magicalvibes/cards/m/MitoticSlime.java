package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "185")
public class MitoticSlime extends Card {

    public MitoticSlime() {
        // When Mitotic Slime dies, create two 2/2 green Ooze creature tokens.
        // They have "When this creature dies, create two 1/1 green Ooze creature tokens."
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                2, "Ooze", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.OOZE), Set.of(), Set.of(),
                Map.of(EffectSlot.ON_DEATH, new CreateTokenEffect(
                        2, "Ooze", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.OOZE), Set.of(), Set.of()
                ))
        ));
    }
}
