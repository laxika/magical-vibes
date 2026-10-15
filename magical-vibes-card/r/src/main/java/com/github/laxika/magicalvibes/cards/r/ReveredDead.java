package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "29")
public class ReveredDead extends Card {

    public ReveredDead() {
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new RegenerateEffect()),
                "{W}: Regenerate Revered Dead."));
    }
}
