package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "157")
@CardRegistration(set = "4ED", collectorNumber = "133")
@CardRegistration(set = "10E", collectorNumber = "139")
@CardRegistration(set = "9ED", collectorNumber = "126")
@CardRegistration(set = "M10", collectorNumber = "95")
@CardRegistration(set = "8ED", collectorNumber = "129")
@CardRegistration(set = "7ED", collectorNumber = "130")
@CardRegistration(set = "6ED", collectorNumber = "123")
public class DrudgeSkeletons extends Card {

    public DrudgeSkeletons() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()), "{B}: Regenerate Drudge Skeletons."));
    }
}
