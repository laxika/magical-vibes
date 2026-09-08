package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnChosenColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;

@CardRegistration(set = "ROE", collectorNumber = "104")
public class CurseOfWizardry extends Card {

    public CurseOfWizardry() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CasterLosesLifeOnChosenColorSpellCastEffect(1));
    }
}
