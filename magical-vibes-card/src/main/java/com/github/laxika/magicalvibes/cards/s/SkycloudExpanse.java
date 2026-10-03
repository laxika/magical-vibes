package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "327")
@CardRegistration(set = "WHO", collectorNumber = "306")
@CardRegistration(set = "WHO", collectorNumber = "516")
@CardRegistration(set = "WHO", collectorNumber = "897")
@CardRegistration(set = "WHO", collectorNumber = "1107")
@CardRegistration(set = "PIP", collectorNumber = "291")
@CardRegistration(set = "PIP", collectorNumber = "509")
@CardRegistration(set = "PIP", collectorNumber = "819")
@CardRegistration(set = "PIP", collectorNumber = "1037")
@CardRegistration(set = "40K", collectorNumber = "294")
@CardRegistration(set = "MKC", collectorNumber = "294")
@CardRegistration(set = "AFC", collectorNumber = "261")
@CardRegistration(set = "C20", collectorNumber = "312")
@CardRegistration(set = "BLC", collectorNumber = "332")
@CardRegistration(set = "NEC", collectorNumber = "177")
@CardRegistration(set = "FIC", collectorNumber = "423")
public class SkycloudExpanse extends Card {

    public SkycloudExpanse() {
        // {1}, {T}: Add {W}{U}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new AwardManaEffect(ManaColor.WHITE),
                        new AwardManaEffect(ManaColor.BLUE)
                ),
                "{1}, {T}: Add {W}{U}."
        ));
    }
}
