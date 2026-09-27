package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;

@CardRegistration(set = "BLB", collectorNumber = "247")
@CardRegistration(set = "SLD", collectorNumber = "2466")
@CardRegistration(set = "SOC", collectorNumber = "353")
@CardRegistration(set = "MSC", collectorNumber = "207")
public class PatchworkBanner extends Card {

    public PatchworkBanner() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenSubtypeEffect(1, 1));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
