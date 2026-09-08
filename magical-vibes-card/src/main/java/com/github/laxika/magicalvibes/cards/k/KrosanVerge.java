package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "141")
public class KrosanVerge extends Card {

    public KrosanVerge() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new Fixed(1),
                                new CardSubtypePredicate(CardSubtype.FOREST),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED,
                                null,
                                1,
                                false,
                                false,
                                false,
                                false,
                                null,
                                LibrarySearchPlayer.CONTROLLER,
                                false,
                                false,
                                false),
                        new SearchLibraryEffect(
                                new CardSubtypePredicate(CardSubtype.PLAINS),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)
                ),
                "{2}, {T}, Sacrifice this land: Search your library for a Forest card and a Plains card, "
                        + "put them onto the battlefield tapped, then shuffle."
        ));
    }
}
