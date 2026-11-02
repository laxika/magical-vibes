package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.FirstNoncreatureSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "266")
public class NullstoneGargoyle extends Card {

    public NullstoneGargoyle() {
        addTrigger(Triggers.playerCastsSpell(),
                new FirstNoncreatureSpellCastTriggerEffect(List.of(new CounterSpellEffect())));
    }
}
