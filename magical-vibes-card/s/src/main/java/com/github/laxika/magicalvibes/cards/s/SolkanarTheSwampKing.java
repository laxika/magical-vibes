package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "85")
@CardRegistration(set = "TSB", collectorNumber = "100")
@CardRegistration(set = "LEG", collectorNumber = "259")
@CardRegistration(set = "DMR", collectorNumber = "200")
public class SolkanarTheSwampKing extends Card {

    public SolkanarTheSwampKing() {
        addTrigger(Triggers.playerCastsSpell(),
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.BLACK), List.of(new GainLifeEffect(1))));
    }
}
