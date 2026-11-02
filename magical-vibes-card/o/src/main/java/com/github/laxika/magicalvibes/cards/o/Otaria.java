package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "OHOP", collectorNumber = "28")
@CardRegistration(set = "OPCA", collectorNumber = "61")
public class Otaria extends Card {

    public Otaria() {
        addStaticEffect(new GrantFlashbackToGraveyardCardsEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY), true));
        addTrigger(Triggers.chaosEnsues(), new ControllerExtraTurnEffect(1));
    }
}
