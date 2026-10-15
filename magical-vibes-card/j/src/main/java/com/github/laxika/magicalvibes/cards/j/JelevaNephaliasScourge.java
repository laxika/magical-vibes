package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.MayCastInstantOrSorceryCardsExiledWithSourceEffect;

@CardRegistration(set = "C13", collectorNumber = "194")
public class JelevaNephaliasScourge extends Card {

    public JelevaNephaliasScourge() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTopCardsToSourceEffect(
                        new ManaSpentToCast(), false, false, LibraryScope.EACH_PLAYER, false));
        addEffect(EffectSlot.ON_ATTACK, new MayCastInstantOrSorceryCardsExiledWithSourceEffect(true));
    }
}
