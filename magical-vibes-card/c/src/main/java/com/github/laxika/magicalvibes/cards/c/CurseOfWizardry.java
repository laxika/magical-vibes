package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnChosenColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "104")
public class CurseOfWizardry extends Card {

    public CurseOfWizardry() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addTrigger(Triggers.playerCastsSpell(),
                new CasterLosesLifeOnChosenColorSpellCastEffect(1));
    }
}
