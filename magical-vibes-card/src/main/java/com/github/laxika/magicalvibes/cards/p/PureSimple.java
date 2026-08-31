package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "154")
public class PureSimple extends Card {

    public PureSimple() {
        TargetFilter multicoloredPermanent = new PermanentPredicateTargetFilter(
                new PermanentIsMulticoloredPredicate(), "Target must be multicolored");
        CardEffect pure = new DestroyTargetPermanentEffect();
        CardEffect simple = new DestroyAllPermanentsEffect(new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.AURA),
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT))));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Pure - Destroy target multicolored permanent",
                        pure,
                        multicoloredPermanent
                ).withManaCost("{1}{R}{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Simple - Destroy all Auras and Equipment",
                        simple
                ).withManaCost("{1}{G}{W}")
        )));
    }
}
