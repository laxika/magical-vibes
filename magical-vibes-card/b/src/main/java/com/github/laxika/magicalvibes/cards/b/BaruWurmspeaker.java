package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "26")
@CardRegistration(set = "DMC", collectorNumber = "76")
public class BaruWurmspeaker extends Card {

    public BaruWurmspeaker() {
        PermanentHasSubtypePredicate wurms = new PermanentHasSubtypePredicate(CardSubtype.WURM);

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, Set.of(Keyword.TRAMPLE),
                GrantScope.ALL_OWN_CREATURES, wurms));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}{G}",
                List.of(
                        new ReduceActivationCostEffect(new GreatestPowerAmongControlled(wurms)),
                        new CreateTokenEffect(
                                "Wurm",
                                4,
                                4,
                                CardColor.GREEN,
                                List.of(CardSubtype.WURM),
                                Set.of(),
                                Set.of())
                ),
                "{7}{G}, {T}: Create a 4/4 green Wurm creature token. This ability costs {X} less to activate, "
                        + "where X is the greatest power among Wurms you control."
        ));
    }
}
