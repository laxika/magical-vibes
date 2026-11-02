package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FCA", collectorNumber = "56")
@CardRegistration(set = "C16", collectorNumber = "34")
public class KraumLudevicsOpus extends Card {

    public KraumLudevicsOpus() {
        addTrigger(Triggers.opponentCastsSpell(),
                new NthSpellCastTriggerEffect(2, List.of(new DrawCardEffect())));
    }
}
