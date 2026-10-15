package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

@CardRegistration(set = "HOU", collectorNumber = "75")
public class RuinRat extends Card {

    public RuinRat() {
        // Deathtouch is auto-loaded from Scryfall.
        // "When this creature dies, exile target card from an opponent's graveyard."
        // The death analogue of Disposal Mummy's ETB; the opponent's-graveyard card is chosen as the
        // trigger goes on the stack (see DeathTriggerCollectorService / processNextDeathTriggerTarget).
        addEffect(EffectSlot.ON_DEATH,
                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD));
    }
}
