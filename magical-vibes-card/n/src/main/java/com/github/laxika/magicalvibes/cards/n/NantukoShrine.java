package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "256")
public class NantukoShrine extends Card {

    public NantukoShrine() {
        addTrigger(Triggers.playerCastsSpell(),
                new CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect());
    }
}
