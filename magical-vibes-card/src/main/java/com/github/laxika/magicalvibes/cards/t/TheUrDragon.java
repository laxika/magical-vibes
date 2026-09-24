package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "361")
@CardRegistration(set = "CMM", collectorNumber = "594")
@CardRegistration(set = "CMM", collectorNumber = "689")
public class TheUrDragon extends Card {

    public TheUrDragon() {
        var otherDragonSpell = new CardAllOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.DRAGON),
                new CardNotPredicate(new CardIsSelfPredicate())));
        var dragonSpellReduction = new ReduceCastCostForMatchingSpellsEffect(
                otherDragonSpell, 1, CostModificationScope.SELF);
        addEffect(EffectSlot.STATIC, dragonSpellReduction);
        addEffect(EffectSlot.COMMAND_ZONE_STATIC, dragonSpellReduction);

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new MinimumMatchingAttackers(1, new PermanentHasSubtypePredicate(CardSubtype.DRAGON)),
                SequenceEffect.of(
                        new DrawCardEffect(new EventValue()),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent"),
                                "Put a permanent card from your hand onto the battlefield?"))));
    }
}
