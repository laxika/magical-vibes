package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "213")
public class BiomechanEngineer extends Card {

    private static final CreateTokenEffect LANDER = CreateTokenEffect.ofArtifactToken(
            1,
            "Lander",
            List.of(CardSubtype.LANDER),
            List.of(new ActivatedAbility(
                    true,
                    "{2}",
                    List.of(
                            new SacrificeSelfCost(),
                            new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                                    LibrarySearchDestination.BATTLEFIELD_TAPPED)
                    ),
                    "{2}, {T}, Sacrifice this token: Search your library for a basic land card, put it onto the battlefield tapped, then shuffle."
            ))
    );

    public BiomechanEngineer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, LANDER);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{8}",
                List.of(
                        new DrawCardEffect(2),
                        new CreateTokenEffect(
                                "Robot", 2, 2, null,
                                List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "{8}: Draw two cards and create a 2/2 colorless Robot artifact creature token."
        ));
    }
}
