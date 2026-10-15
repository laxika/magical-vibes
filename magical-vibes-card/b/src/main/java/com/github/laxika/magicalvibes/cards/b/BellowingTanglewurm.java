package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "111")
public class BellowingTanglewurm extends Card {

    public BellowingTanglewurm() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.INTIMIDATE, GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));
    }
}
