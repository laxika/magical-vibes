package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YONE", collectorNumber = "1")
public class GhalmaTheShaper extends Card {

    public GhalmaTheShaper() {
        addEffect(EffectSlot.ON_ATTACK, new ConjureCardNamedIntoHandEffect("Tempered Steel", false));
        addEffect(EffectSlot.ON_ATTACK,
                new CreateTokenEffect(1, "Myr", 1, 1, null,
                        List.of(CardSubtype.MYR), Set.of(), Set.of(CardType.ARTIFACT)));
    }
}
