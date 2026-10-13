package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTriggeringPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfEachOpponentEffect;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "203")
public class FathomFeeder extends Card {

    public FathomFeeder() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ExileTopCardOfTriggeringPlayerLibraryEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}{B}",
                List.of(
                        new DrawCardEffect(1),
                        new ExileTopCardsOfEachOpponentEffect(1)
                ),
                "{3}{U}{B}: Draw a card. Each opponent exiles the top card of their library."
        ));
    }
}
