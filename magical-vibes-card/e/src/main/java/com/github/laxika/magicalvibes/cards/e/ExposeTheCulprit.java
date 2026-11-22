package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFaceDownPileEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetFaceUpEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "124")
@CardRegistration(set = "MKM", collectorNumber = "307")
public class ExposeTheCulprit extends Card {

    public ExposeTheCulprit() {
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Turn target face-down creature face up",
                        new TurnTargetFaceUpEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile any number of face-up creatures you control with disguise in a face-down pile, shuffle that pile, then cloak them",
                        ExileFaceDownPileEffect.thenCloak(new ExileFaceDownPileEffect.AnyNumberYouControl(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentIsFaceDownPredicate()),
                                        new PermanentHasKeywordPredicate(Keyword.DISGUISE))),
                                "face-up creatures with disguise")))
        )));
    }
}
