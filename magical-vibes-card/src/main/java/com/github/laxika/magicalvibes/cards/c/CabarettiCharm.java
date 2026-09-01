package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "173")
public class CabarettiCharm extends Card {

    public CabarettiCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Cabaretti Charm deals damage equal to the number of creatures you control to target creature or planeswalker",
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(new PermanentCount(
                                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)),
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate())),
                                "Target must be a creature or planeswalker.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+1 and gain trample until end of turn",
                        List.of(
                                new BoostAllOwnCreaturesEffect(1, 1),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES))),
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 1/1 green and white Citizen creature tokens",
                        new CreateTokenEffect(2, "Citizen", 1, 1, CardColor.GREEN,
                                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN)))
        )));
    }
}
