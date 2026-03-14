package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "119")
public class WallOfBone extends Card {

    public WallOfBone() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()), "{B}: Regenerate Wall of Bone."));
    }
}
