package com.github.laxika.magicalvibes.cards.a;

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
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "250")
public class AxgardArmory extends Card {

    public AxgardArmory() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}{R}{W}",
                List.of(
                        new SacrificeSelfCost(),
                        searchForSubtypeWithoutShuffling(CardSubtype.AURA),
                        searchForSubtypeWithoutShuffling(CardSubtype.EQUIPMENT),
                        new ShuffleLibraryEffect(false)
                ),
                "{1}{R}{R}{W}, {T}, Sacrifice this land: Search your library for an Aura card and/or an Equipment card, reveal them, put them into your hand, then shuffle."
        ));
    }

    private static SearchLibraryEffect searchForSubtypeWithoutShuffling(CardSubtype subtype) {
        return new SearchLibraryEffect(
                new Fixed(1),
                new CardSubtypePredicate(subtype),
                LibrarySearchDestination.HAND,
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
                false
        );
    }
}
