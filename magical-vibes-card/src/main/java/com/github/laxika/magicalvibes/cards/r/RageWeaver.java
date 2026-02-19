package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CreatureColorTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "223")
public class RageWeaver extends Card {

    public RageWeaver() {
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new GrantKeywordToTargetEffect(Keyword.HASTE)), true, "{2}: Target black or green creature gains haste until end of turn.", new CreatureColorTargetFilter(Set.of(CardColor.BLACK, CardColor.GREEN))));
    }
}
