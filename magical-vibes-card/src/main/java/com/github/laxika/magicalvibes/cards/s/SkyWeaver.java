package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CreatureColorTargetFilter;

import java.util.List;
import java.util.Set;

public class SkyWeaver extends Card {

    public SkyWeaver() {
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new GrantKeywordToTargetEffect(Keyword.FLYING)), true, "{2}: Target white or black creature gains flying until end of turn.", new CreatureColorTargetFilter(Set.of(CardColor.WHITE, CardColor.BLACK))));
    }
}
