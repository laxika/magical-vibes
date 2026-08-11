package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "2")
public class AjaniStrengthOfThePride extends Card {

    public AjaniStrengthOfThePride() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new GainLifeEffect(new Sum(
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                        new PermanentCount(new PermanentIsPlaneswalkerPredicate(), CountScope.CONTROLLER)))),
                "+1: You gain life equal to the number of creatures you control plus the number of planeswalkers you control."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        1, "Ajani's Pridemate", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.CAT, CardSubtype.SOLDIER), Set.of(), Set.of(),
                        Map.of(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new PutCountersOnSourceEffect(1, 1, 1)))),
                "−2: Create a 2/2 white Cat Soldier creature token named Ajani's Pridemate with \"Whenever you gain life, put a +1/+1 counter on this token.\""
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new ConditionalEffect(
                        new ControllerLifeAtLeast(GameData.STARTING_LIFE_TOTAL + 15),
                        new ExileAllPermanentsEffect(new PermanentAnyOfPredicate(List.of(
                                new PermanentIsSourceCardPredicate(),
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentIsArtifactPredicate(),
                                                new PermanentIsCreaturePredicate()))))))))),
                "0: If you have at least 15 life more than your starting life total, exile Ajani and each artifact and creature your opponents control."
        ));
    }
}
