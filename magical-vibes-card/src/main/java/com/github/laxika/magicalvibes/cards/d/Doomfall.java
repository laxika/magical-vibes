package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCreatureExileEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "62")
public class Doomfall extends Card {

    public Doomfall() {
        // Both modes target an opponent (player-targeting spec on each effect).
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneOption("Target opponent exiles a creature they control.",
                        new TargetPlayerChoosesCreatureExileEffect()),
                new ChooseOneOption("Target opponent reveals their hand. You choose a nonland card from it. Exile that card.",
                        new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), HandChoiceDestination.EXILE)))));
    }
}
