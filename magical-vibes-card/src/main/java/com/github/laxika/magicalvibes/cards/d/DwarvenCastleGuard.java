package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "18")
public class DwarvenCastleGuard extends Card {

    public DwarvenCastleGuard() {
        // When this creature dies, create a 1/1 colorless Hero creature token.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Hero", 1, 1, null,
                List.of(CardSubtype.HERO), Set.of(), Set.of()));
    }
}
