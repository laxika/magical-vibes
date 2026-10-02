package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "328")
@CardRegistration(set = "WHO", collectorNumber = "311")
@CardRegistration(set = "WHO", collectorNumber = "521")
@CardRegistration(set = "WHO", collectorNumber = "902")
@CardRegistration(set = "WHO", collectorNumber = "1112")
@CardRegistration(set = "PIP", collectorNumber = "295")
@CardRegistration(set = "PIP", collectorNumber = "513")
@CardRegistration(set = "PIP", collectorNumber = "823")
@CardRegistration(set = "PIP", collectorNumber = "1041")
@CardRegistration(set = "MSC", collectorNumber = "270")
@CardRegistration(set = "MSC", collectorNumber = "497")
@CardRegistration(set = "MKC", collectorNumber = "297")
@CardRegistration(set = "AFC", collectorNumber = "264")
@CardRegistration(set = "C20", collectorNumber = "317")
@CardRegistration(set = "BLC", collectorNumber = "334")
@CardRegistration(set = "MIC", collectorNumber = "181")
@CardRegistration(set = "C19", collectorNumber = "277")
@CardRegistration(set = "ONC", collectorNumber = "166")
public class SungrassPrairie extends Card {

    public SungrassPrairie() {
        // {1}, {T}: Add {G}{W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.GREEN), new AwardManaEffect(ManaColor.WHITE)),
                "{1}, {T}: Add {G}{W}."
        ));
    }
}
