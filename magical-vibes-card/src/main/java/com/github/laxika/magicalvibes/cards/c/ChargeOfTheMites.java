package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "6")
public class ChargeOfTheMites extends Card {

    public ChargeOfTheMites() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Charge of the Mites deals damage equal to the number of creatures you control to target creature or planeswalker",
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)),
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate())),
                                "Target must be a creature or planeswalker."
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 1/1 colorless Phyrexian Mite artifact creature tokens with toxic 1 and this token can't block",
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                2,
                                "Mite",
                                1,
                                1,
                                null,
                                null,
                                List.of(CardSubtype.PHYREXIAN, CardSubtype.MITE),
                                Set.of(Keyword.TOXIC),
                                Set.of(CardType.ARTIFACT),
                                false,
                                false,
                                Map.of(
                                        EffectSlot.STATIC, new CantBlockEffect(),
                                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                        new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER)
                                ),
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of()
                        ))
        )));
    }
}
