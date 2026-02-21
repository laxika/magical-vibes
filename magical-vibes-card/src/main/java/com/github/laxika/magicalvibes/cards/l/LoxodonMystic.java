package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "26")
public class LoxodonMystic extends Card {

    public LoxodonMystic() {
        addActivatedAbility(new ActivatedAbility(true, "{W}", List.of(new TapTargetPermanentEffect(Set.of(CardType.CREATURE))), true, "{W}, {T}: Tap target creature."));
    }
}
