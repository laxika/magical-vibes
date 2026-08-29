package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeDistinctNamePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "BIG", collectorNumber = "28")
public class TransmutationFont extends Card {

    public TransmutationFont() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Create a Blood token", CreateTokenEffect.ofBloodToken(1)),
                        new ChooseOneEffect.ChooseOneOption("Create a Clue token", CreateTokenEffect.ofClueToken(1)),
                        new ChooseOneEffect.ChooseOneOption("Create a Food token", foodToken())
                ))),
                "{T}: Create your choice of a Blood token, a Clue token, or a Food token."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new SacrificeDistinctNamePermanentsCost(3, new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(), new PermanentIsTokenPredicate()))),
                        new SearchLibraryEffect(new CardTypePredicate(CardType.ARTIFACT), LibrarySearchDestination.BATTLEFIELD)
                ),
                "{3}, {T}, Sacrifice three artifact tokens with different names: Search your library for an artifact card, put it onto the battlefield, then shuffle. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )));
    }
}
