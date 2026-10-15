package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "108")
public class ZerapaMinotaur extends Card {

    public ZerapaMinotaur() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new RemoveKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{2}: This creature loses first strike until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
