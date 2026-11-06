package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "HBG", collectorNumber = "186")
public class Nalfeshnee extends Card {

    public Nalfeshnee() {
        addTrigger(Triggers.youCastSpell().anySpell().castFrom(Zone.EXILE),
                new CopyTriggeringSpellEffect().permanentSpellCopiesAreTokens().permanentSpellCopiesGainHaste()
                        .sacrificedAtEndStep());
    }
}
