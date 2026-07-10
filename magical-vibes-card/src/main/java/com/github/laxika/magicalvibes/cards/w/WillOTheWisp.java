package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "169")
public class WillOTheWisp extends Card {

    public WillOTheWisp() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()), "{B}: Regenerate Will-o'-the-Wisp."));
    }
}
