package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "19")
public class RallyTheMonastery extends Card {

    public RallyTheMonastery() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()),
                new ReduceOwnCastCostEffect(new Fixed(2))));

        Map<EffectSlot, CardEffect> monkTokenEffects = Map.of(
                EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new BoostSelfEffect(1, 1))));
        CreateTokenEffect monkToken = new CreateTokenEffect(
                CardType.CREATURE, 2, "Monk", 1, 1,
                CardColor.WHITE, null, List.of(CardSubtype.MONK),
                Set.of(), Set.of(), false, false, monkTokenEffects, List.of(),
                false, false, false, 0, Set.of());

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 1/1 white Monk creature tokens with prowess",
                        monkToken),
                new ChooseOneEffect.ChooseOneOption(
                        "Up to two target creatures you control each get +2/+2 until end of turn",
                        List.<CardEffect>of(new BoostTargetCreatureEffect(2, 2)),
                        TargetFilters.creatureYouControl(), null, 0, 2, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature with power 4 or greater",
                        DestroyTargetPermanentEffect.forTargetGroup(0),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentPowerAtLeastPredicate(4))),
                                "Target must be a creature with power 4 or greater"))
        )));
    }
}
