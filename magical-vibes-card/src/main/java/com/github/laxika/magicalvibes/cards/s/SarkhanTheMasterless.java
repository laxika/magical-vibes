package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDealDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "143")
public class SarkhanTheMasterless extends Card {

    public SarkhanTheMasterless() {
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new ControlledCreaturesDealDamageToTargetEffect(1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.DRAGON)
                        ))));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new AnimatePermanentsEffect(
                        new Fixed(4), new Fixed(4), List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), CardColor.RED,
                        Set.of(CardType.CREATURE), GrantScope.OWN_PERMANENTS,
                        EffectDuration.UNTIL_END_OF_TURN, new PermanentIsPlaneswalkerPredicate(), true
                )),
                "+1: Until end of turn, each planeswalker you control becomes a 4/4 red Dragon creature and gains flying."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new CreateTokenEffect("Dragon", 4, 4, CardColor.RED,
                        List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of())),
                "−3: Create a 4/4 red Dragon creature token with flying."
        ));
    }
}
