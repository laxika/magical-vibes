package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "61")
public class GeralfVisionaryStitcher extends Card {

    public GeralfVisionaryStitcher() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 0, Set.of(Keyword.FLYING),
                GrantScope.OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                                )),
                                "another nontoken creature",
                                true,
                                false,
                                false,
                                true),
                        new CreateTokenEffect("Zombie", new XValue(), new XValue(), CardColor.BLUE,
                                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of())
                ),
                "{U}, {T}, Sacrifice another nontoken creature: Create an X/X blue Zombie creature token, "
                        + "where X is the sacrificed creature's toughness."
        ));
    }
}
