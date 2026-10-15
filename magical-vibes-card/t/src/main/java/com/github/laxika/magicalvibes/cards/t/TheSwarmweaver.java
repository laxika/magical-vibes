package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "236")
public class TheSwarmweaver extends Card {

    public TheSwarmweaver() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Insect", 1, 1, CardColor.BLACK,
                        Set.of(CardColor.BLACK, CardColor.GREEN), List.of(CardSubtype.INSECT),
                        Set.of(Keyword.FLYING), Set.of()));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(), new StaticBoostEffect(1, 1, Set.of(Keyword.DEATHTOUCH),
                        GrantScope.OWN_CREATURES,
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.INSECT, CardSubtype.SPIDER)))));
    }
}
