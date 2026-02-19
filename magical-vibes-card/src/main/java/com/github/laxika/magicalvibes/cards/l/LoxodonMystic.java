package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapTargetCreatureEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "26")
public class LoxodonMystic extends Card {

    public LoxodonMystic() {
        addActivatedAbility(new ActivatedAbility(true, "{W}", List.of(new TapTargetCreatureEffect()), true, "{W}, {T}: Tap target creature."));
    }
}
