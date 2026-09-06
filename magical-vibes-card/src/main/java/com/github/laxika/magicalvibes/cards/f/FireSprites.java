package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "186")
public class FireSprites extends Card {

    public FireSprites() {
        addActivatedAbility(new ActivatedAbility(true, "{G}",
                List.of(new AwardManaEffect(ManaColor.RED)),
                "{G}, {T}: Add {R}."));
    }
}
