package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "75")
@CardRegistration(set = "VMA", collectorNumber = "142")
@CardRegistration(set = "SLD", collectorNumber = "2004")
@CardRegistration(set = "SLD", collectorNumber = "2023")
@CardRegistration(set = "STA", collectorNumber = "34")
@CardRegistration(set = "MB2", collectorNumber = "50")
public class TendrilsOfAgony extends Card {

    public TendrilsOfAgony() {
        addSpellEffect(new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));
        addSpellEffect(new GainLifeEffect(2));
        addTrigger(Triggers.youCastThisSpell(), new StormEffect());
    }
}
