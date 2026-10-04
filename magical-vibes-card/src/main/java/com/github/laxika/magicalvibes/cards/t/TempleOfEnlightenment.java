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

@CardRegistration(set = "BNG", collectorNumber = "163")
@CardRegistration(set = "THB", collectorNumber = "246")
@CardRegistration(set = "WHO", collectorNumber = "315")
@CardRegistration(set = "WHO", collectorNumber = "525")
@CardRegistration(set = "WHO", collectorNumber = "906")
@CardRegistration(set = "WHO", collectorNumber = "1116")
@CardRegistration(set = "PIP", collectorNumber = "304")
@CardRegistration(set = "PIP", collectorNumber = "518")
@CardRegistration(set = "PIP", collectorNumber = "832")
@CardRegistration(set = "PIP", collectorNumber = "1046")
@CardRegistration(set = "DSC", collectorNumber = "308")
@CardRegistration(set = "MKC", collectorNumber = "302")
@CardRegistration(set = "BLC", collectorNumber = "339")
@CardRegistration(set = "FIC", collectorNumber = "435")
@CardRegistration(set = "EOC", collectorNumber = "186")
@CardRegistration(set = "VOC", collectorNumber = "185")
@CardRegistration(set = "BRC", collectorNumber = "206")
@CardRegistration(set = "SCD", collectorNumber = "325")
public class TempleOfEnlightenment extends Card {

    public TempleOfEnlightenment() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLUE))),
                "{T}: Add {W} or {U}."
        ));
    }
}
