package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "SUM", collectorNumber = "262")
@CardRegistration(set = "3ED", collectorNumber = "262")
@CardRegistration(set = "ME4", collectorNumber = "212")
@CardRegistration(set = "2ED", collectorNumber = "259")
public class LivingWall extends Card {

    public LivingWall() {
        addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(new RegenerateEffect()), "{1}: Regenerate Living Wall."));
    }
}
