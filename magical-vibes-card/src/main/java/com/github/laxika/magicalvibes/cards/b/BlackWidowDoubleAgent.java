package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "208")
public class BlackWidowDoubleAgent extends Card {

    public BlackWidowDoubleAgent() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(),
                        new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.MENACE), GrantScope.TARGET)));
    }
}
