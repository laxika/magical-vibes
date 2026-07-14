package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "235")
@CardRegistration(set = "7ED", collectorNumber = "250")
public class GorillaChieftain extends Card {

    public GorillaChieftain() {
        // {1}{G}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new RegenerateEffect()),
                "{1}{G}: Regenerate Gorilla Chieftain."));
    }
}
