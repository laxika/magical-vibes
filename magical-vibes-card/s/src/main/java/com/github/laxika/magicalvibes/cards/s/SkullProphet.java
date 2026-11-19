package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "IKO", collectorNumber = "206")
@CardRegistration(set = "TDC", collectorNumber = "304")
public class SkullProphet extends Card {

    public SkullProphet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN))),
                "{T}: Add {B} or {G}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillEffect(2, Subjects.you())),
                "{T}: Mill two cards."
        ));
    }
}
