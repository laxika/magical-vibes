package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "87")
public class KariZevSkyshipRaider extends Card {

    public KariZevSkyshipRaider() {
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                CardType.CREATURE, 1, "Ragavan", 2, 1, CardColor.RED, null,
                List.of(CardSubtype.MONKEY), Set.of(), Set.of(),
                true, false, Map.of(), List.of(),
                true, false, true, 0, Set.of()
        ));
    }
}
