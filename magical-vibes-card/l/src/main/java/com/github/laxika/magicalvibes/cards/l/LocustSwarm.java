package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "226")
public class LocustSwarm extends Card {

    public LocustSwarm() {
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new RegenerateEffect()),
                "{G}: Regenerate this creature."));
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{G}: Untap this creature. Activate only once each turn.", 1));
    }
}
