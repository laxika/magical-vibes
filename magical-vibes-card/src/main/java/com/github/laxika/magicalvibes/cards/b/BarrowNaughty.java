package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "81")
public class BarrowNaughty extends Card {

    public BarrowNaughty() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsAnotherPermanent(new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FAERIE))),
                new StaticBoostEffect(0, 0, Set.of(Keyword.LIFELINK), GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new BoostSelfEffect(1, 0)),
                "{2}{B}: This creature gets +1/+0 until end of turn."
        ));
    }
}
