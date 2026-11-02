package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PayLifeInsteadOfCommanderTaxEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1961")
public class LiesaShroudOfDusk extends Card {

    public LiesaShroudOfDusk() {
        addStaticEffect(new PayLifeInsteadOfCommanderTaxEffect(2));
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                null, List.of(new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER))));
    }
}
