package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "164")
@CardRegistration(set = "THB", collectorNumber = "247")
@CardRegistration(set = "WHO", collectorNumber = "317")
@CardRegistration(set = "WHO", collectorNumber = "527")
@CardRegistration(set = "WHO", collectorNumber = "908")
@CardRegistration(set = "WHO", collectorNumber = "1118")
@CardRegistration(set = "PIP", collectorNumber = "307")
@CardRegistration(set = "PIP", collectorNumber = "521")
@CardRegistration(set = "PIP", collectorNumber = "835")
@CardRegistration(set = "PIP", collectorNumber = "1049")
@CardRegistration(set = "DSC", collectorNumber = "310")
@CardRegistration(set = "VOC", collectorNumber = "186")
public class TempleOfMalice extends Card {

    public TempleOfMalice() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.RED))),
                "{T}: Add {B} or {R}."
        ));
    }
}
