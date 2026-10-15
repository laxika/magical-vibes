package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "211")
public class TrackDown extends Card {

    public TrackDown() {
        // Scry 3, then reveal the top card of your library. If it's a creature or land card, draw a card.
        addEffect(EffectSlot.SPELL, new ScryEffect(3));
        addEffect(EffectSlot.SPELL, new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AnyOf(List.of(
                        new TopCardOfLibraryType(CardType.CREATURE),
                        new TopCardOfLibraryType(CardType.LAND))),
                new DrawCardEffect()));
    }
}
