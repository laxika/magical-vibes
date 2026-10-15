package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "240")
public class WoodwraithCorrupter extends Card {

    public WoodwraithCorrupter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}{G}",
                List.of(new AnimatePermanentsEffect(
                        new Fixed(4), new Fixed(4),
                        List.of(CardSubtype.ELEMENTAL, CardSubtype.HORROR),
                        Set.of(),
                        null, Set.of(),
                        GrantScope.TARGET, EffectDuration.PERMANENT, null,
                        Set.of(CardColor.BLACK, CardColor.GREEN)
                )),
                "{1}{B}{G}, {T}: Target Forest becomes a 4/4 black and green Elemental Horror creature. It's still a land.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                        "Target must be a Forest"
                )
        ));
    }
}
