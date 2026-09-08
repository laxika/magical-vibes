package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "229")
public class ThrashThreat extends Card {

    public ThrashThreat() {
        TargetFilter creatureYouControl = TargetFilters.creatureYouControl();
        TargetFilter creatureOrPlaneswalkerYouDoNotControl = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsPlaneswalkerPredicate()
                        )),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target must be a creature or planeswalker you don't control"
        );

        CardEffect thrash = new TargetCreatureDealsPowerDamageToAnyTargetEffect();
        CardEffect threat = new CreateTokenEffect(
                1, "Beast", 4, 4, CardColor.RED, Set.of(CardColor.RED, CardColor.GREEN),
                List.of(CardSubtype.BEAST), Set.of(Keyword.TRAMPLE), Set.of());

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Thrash — Target creature you control deals damage equal to its power to target creature or planeswalker you don't control",
                        List.<CardEffect>of(thrash),
                        List.of(creatureYouControl, creatureOrPlaneswalkerYouDoNotControl)
                ).withManaCost("{R/G}{R/G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Threat — Create a 4/4 red and green Beast creature token with trample",
                        threat
                ).withManaCost("{2}{R}{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Thrash and then Threat",
                        List.of(thrash, threat),
                        List.of(creatureYouControl, creatureOrPlaneswalkerYouDoNotControl)
                ).withManaCost("{2}{R/G}{R/G}{R}{G}")
        )));
    }
}
