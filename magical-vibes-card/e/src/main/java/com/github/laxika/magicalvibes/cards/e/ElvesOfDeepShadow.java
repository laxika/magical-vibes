package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "76")
@CardRegistration(set = "RAV", collectorNumber = "161")
@CardRegistration(set = "DDJ", collectorNumber = "47")
@CardRegistration(set = "ME3", collectorNumber = "116")
@CardRegistration(set = "SLD", collectorNumber = "1710")
@CardRegistration(set = "GK1", collectorNumber = "56")
public class ElvesOfDeepShadow extends Card {

    public ElvesOfDeepShadow() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.BLACK),
                        DealDamageEffect.to(Subjects.you(), 1)
                ),
                "{T}: Add {B}. This creature deals 1 damage to you."
        ));
    }
}
