package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "62")
public class Galvanoth extends Card {

    public Galvanoth() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new CastTopOfLibraryWithoutPayingManaCostEffect(Set.of(CardType.INSTANT, CardType.SORCERY)),
                "Look at the top card of your library?"
        ));
    }
}
