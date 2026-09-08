package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "155")
@CardRegistration(set = "TPR", collectorNumber = "116")
public class ScreechingHarpy extends Card {

    public ScreechingHarpy() {
        // {1}{B}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Screeching Harpy."));
    }
}
