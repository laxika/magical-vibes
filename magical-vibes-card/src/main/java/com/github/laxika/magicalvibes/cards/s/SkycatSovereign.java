package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "207")
public class SkycatSovereign extends Card {

    public SkycatSovereign() {
        PermanentCount otherFlyingCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING)
                )), CountScope.CONTROLLER, true);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                otherFlyingCreatures, otherFlyingCreatures, GrantScope.SELF));

        addActivatedAbility(new ActivatedAbility(
                false, "{2}{W}{U}",
                List.of(new CreateTokenEffect("Cat Bird", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.CAT, CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())),
                "{2}{W}{U}: Create a 1/1 white Cat Bird creature token with flying."
        ));
    }
}
