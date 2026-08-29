package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "155")
public class SanguineGuard extends Card {

    public SanguineGuard() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Sanguine Guard."));
    }
}
