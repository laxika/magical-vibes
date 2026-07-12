package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "290")
@CardRegistration(set = "9ED", collectorNumber = "265")
@CardRegistration(set = "8ED", collectorNumber = "275")
public class Regeneration extends Card {

    public Regeneration() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new RegenerateEffect()),
                "{G}: Regenerate enchanted creature."
        ));
    }
}
