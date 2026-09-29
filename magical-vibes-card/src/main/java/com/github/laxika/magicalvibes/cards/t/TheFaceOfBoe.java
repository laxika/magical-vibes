package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayCastSpellWithSuspendCostFromHandEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "126")
public class TheFaceOfBoe extends Card {

    public TheFaceOfBoe() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MayCastSpellWithSuspendCostFromHandEffect()),
                "{T}: You may cast a spell with suspend from your hand. If you do, pay its suspend cost rather than its mana cost. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
