package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "46")
public class RibbonSnake extends Card {

    public RibbonSnake() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new RemoveKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{2}: This creature loses flying until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
