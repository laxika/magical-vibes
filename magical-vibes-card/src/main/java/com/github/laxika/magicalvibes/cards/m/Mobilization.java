package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "29")
public class Mobilization extends Card {

    public Mobilization() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 0, Set.of(Keyword.VIGILANCE), GrantScope.ALL_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SOLDIER))));
        addActivatedAbility(new ActivatedAbility(false, "{2}{W}", List.of(CreateTokenEffect.whiteSoldier(1)), "{2}{W}: Create a 1/1 white Soldier creature token."));
    }
}
