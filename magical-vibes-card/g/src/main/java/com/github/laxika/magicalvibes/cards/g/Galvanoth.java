package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "62")
@CardRegistration(set = "DDJ", collectorNumber = "10")
public class Galvanoth extends Card {

    public Galvanoth() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new CastTopOfLibraryWithoutPayingManaCostEffect(Set.of(CardType.INSTANT, CardType.SORCERY)),
                "Look at the top card of your library?"
        ));
    }
}
