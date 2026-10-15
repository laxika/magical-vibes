package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesForManaCost;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "246")
public class HeirloomEpic extends Card {

    public HeirloomEpic() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new TapCreaturesForManaCost(), new DrawCardEffect(1)),
                "{4}, {T}: Draw a card. For each mana in this ability's activation cost, you may tap an untapped creature you control rather than pay that mana. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
