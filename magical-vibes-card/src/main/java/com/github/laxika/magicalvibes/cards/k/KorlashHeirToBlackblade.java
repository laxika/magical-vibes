package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "87")
public class KorlashHeirToBlackblade extends Card {

    public KorlashHeirToBlackblade() {
        PermanentCount swampsYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(swampsYouControl, swampsYouControl));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Korlash."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(
                                new CardNamedPredicate("Korlash, Heir to Blackblade"),
                                "Korlash, Heir to Blackblade"
                        ),
                        new SearchLibraryEffect(
                                new Fixed(2),
                                new CardSubtypePredicate(CardSubtype.SWAMP),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)
                ),
                "Grandeur — Discard another card named Korlash, Heir to Blackblade: Search your library "
                        + "for up to two Swamp cards, put them onto the battlefield tapped, then shuffle."
        ));
    }
}
