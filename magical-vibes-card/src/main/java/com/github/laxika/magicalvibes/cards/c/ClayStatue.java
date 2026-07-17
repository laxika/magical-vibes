package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "355")
public class ClayStatue extends Card {

    public ClayStatue() {
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new RegenerateEffect()), "{2}: Regenerate Clay Statue."));
    }
}
