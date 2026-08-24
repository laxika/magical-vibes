package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "249")
public class MobilizedDistrict extends Card {

    public MobilizedDistrict() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        PermanentAllOfPredicate legendaryCreature = new PermanentAllOfPredicate(List.of(
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                new PermanentIsCreaturePredicate()));
        PermanentCount legendaryCreatureOrPlaneswalker = new PermanentCount(
                new PermanentAnyOfPredicate(List.of(legendaryCreature, new PermanentIsPlaneswalkerPredicate())),
                CountScope.CONTROLLER);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new ReduceActivationCostEffect(legendaryCreatureOrPlaneswalker),
                        new AnimatePermanentsEffect(3, 3, List.of(CardSubtype.CITIZEN), Set.of(Keyword.VIGILANCE))
                ),
                "{4}: This land becomes a 3/3 Citizen creature with vigilance until end of turn. It's still a land. "
                        + "This ability costs {1} less to activate for each legendary creature and planeswalker you control."
        ));
    }
}
