package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "97")
public class GraveTitan extends Card {

    public GraveTitan() {
        // Whenever Grave Titan enters the battlefield or attacks,
        // create two 2/2 black Zombie creature tokens.
        CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                2, "Zombie", 2, 2, CardColor.BLACK, List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()
        );
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, tokenEffect);
        addEffect(EffectSlot.ON_ATTACK, tokenEffect);
    }
}
