package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "119")
@CardRegistration(set = "7ED", collectorNumber = "169")
@CardRegistration(set = "5ED", collectorNumber = "203")
@CardRegistration(set = "4ED", collectorNumber = "168")
@CardRegistration(set = "ITP", collectorNumber = "27")
@CardRegistration(set = "RQS", collectorNumber = "26")
@CardRegistration(set = "SUM", collectorNumber = "134")
@CardRegistration(set = "3ED", collectorNumber = "134")
@CardRegistration(set = "DDD", collectorNumber = "41")
@CardRegistration(set = "GVL", collectorNumber = "41")
public class WallOfBone extends Card {

    public WallOfBone() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()), "{B}: Regenerate Wall of Bone."));
    }
}
