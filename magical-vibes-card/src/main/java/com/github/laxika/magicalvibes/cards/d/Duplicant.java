package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreaturePower;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreatureToughness;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetCreatureTypesToImprintedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "165")
public class Duplicant extends Card {

    public Duplicant() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                )),
                "Target must be a nontoken creature"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ExileTargetPermanentAndImprintEffect(),
                "Exile target nontoken creature?"
        ));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ImprintedCardMatches(new CardTypePredicate(CardType.CREATURE), "a creature card"),
                new SetPowerToughnessToAmountEffect(
                        new ImprintedCreaturePower(), new ImprintedCreatureToughness())));
        addEffect(EffectSlot.STATIC,
                new SetCreatureTypesToImprintedCreatureEffect(CardSubtype.SHAPESHIFTER));
    }
}
