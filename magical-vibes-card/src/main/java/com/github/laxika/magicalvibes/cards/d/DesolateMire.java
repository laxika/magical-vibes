package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "146")
@CardRegistration(set = "PIP", collectorNumber = "438")
@CardRegistration(set = "PIP", collectorNumber = "674")
@CardRegistration(set = "PIP", collectorNumber = "966")
public class DesolateMire extends Card {

    public DesolateMire() {
        // {1}, {T}: Add {W}{B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.WHITE), new AwardManaEffect(ManaColor.BLACK)),
                "{1}, {T}: Add {W}{B}."
        ));
    }
}
