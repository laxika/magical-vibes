package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "277")
@CardRegistration(set = "C14", collectorNumber = "221")
@CardRegistration(set = "CMA", collectorNumber = "158")
public class TitaniasChosen extends Card {

    public TitaniasChosen() {
        addTrigger(Triggers.playerCastsSpell(),
                new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                        Set.of(CardColor.GREEN), 1, false));
    }
}
