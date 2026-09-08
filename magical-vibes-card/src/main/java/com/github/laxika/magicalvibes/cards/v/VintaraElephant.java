package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "131")
public class VintaraElephant extends Card {

    public VintaraElephant() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new RemoveKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "{3}: This creature loses trample until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
