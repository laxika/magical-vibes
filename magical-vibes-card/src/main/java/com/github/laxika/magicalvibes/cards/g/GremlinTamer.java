package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "215")
public class GremlinTamer extends Card {

    public GremlinTamer() {
        CreateTokenEffect gremlin = new CreateTokenEffect(
                "Gremlin", 1, 1, CardColor.RED, List.of(CardSubtype.GREMLIN), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, gremlin);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, gremlin);
    }
}
