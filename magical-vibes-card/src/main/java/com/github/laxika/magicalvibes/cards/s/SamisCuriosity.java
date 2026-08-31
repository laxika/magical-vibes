package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "203")
public class SamisCuriosity extends Card {

    private static final CreateTokenEffect LANDER = CreateTokenEffect.ofArtifactToken(
            1,
            "Lander",
            List.of(CardSubtype.LANDER),
            List.of(new ActivatedAbility(
                    true,
                    "{2}",
                    List.of(
                            new SacrificeSelfCost(),
                            new SearchLibraryEffect(
                                    CardPredicateUtils.basicLand(),
                                    LibrarySearchDestination.BATTLEFIELD_TAPPED)
                    ),
                    "{2}, {T}, Sacrifice this token: Search your library for a basic land card, put it onto the battlefield tapped, then shuffle."
            ))
    );

    public SamisCuriosity() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
        addEffect(EffectSlot.SPELL, LANDER);
    }
}
