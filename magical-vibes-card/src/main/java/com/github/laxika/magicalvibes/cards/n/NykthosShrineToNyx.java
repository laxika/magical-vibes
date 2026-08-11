package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfChosenColorEqualToDevotionEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "223")
public class NykthosShrineToNyx extends Card {

    public NykthosShrineToNyx() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new AwardManaOfChosenColorEqualToDevotionEffect()),
                "{2}, {T}: Choose a color. Add an amount of mana of that color equal to your devotion to that color."
        ));
    }
}
