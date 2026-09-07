package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "151")
public class PurphorossIntervention extends Card {

    public PurphorossIntervention() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create an X/1 red Elemental creature token with trample and haste. Sacrifice it at the beginning of the next end step",
                        List.of(
                                new CreateTokenEffect("Elemental", new XValue(), new Fixed(1), CardColor.RED,
                                        List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of()),
                                new SacrificeCreatedPermanentsAtEndStepEffect()
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Purphoros's Intervention deals twice X damage to target creature or planeswalker",
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(new Scaled(new XValue(), 2)),
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate()
                                )),
                                "Target must be a creature or planeswalker"
                        ))
        )));
    }
}
