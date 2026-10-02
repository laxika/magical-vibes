package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "THS", collectorNumber = "225")
@CardRegistration(set = "THB", collectorNumber = "245")
@CardRegistration(set = "WHO", collectorNumber = "314")
@CardRegistration(set = "WHO", collectorNumber = "524")
@CardRegistration(set = "WHO", collectorNumber = "905")
@CardRegistration(set = "WHO", collectorNumber = "1115")
@CardRegistration(set = "PIP", collectorNumber = "303")
@CardRegistration(set = "PIP", collectorNumber = "517")
@CardRegistration(set = "PIP", collectorNumber = "831")
@CardRegistration(set = "PIP", collectorNumber = "1045")
@CardRegistration(set = "DSC", collectorNumber = "307")
@CardRegistration(set = "MIC", collectorNumber = "184")
@CardRegistration(set = "WOC", collectorNumber = "170")
public class TempleOfDeceit extends Card {

    public TempleOfDeceit() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
