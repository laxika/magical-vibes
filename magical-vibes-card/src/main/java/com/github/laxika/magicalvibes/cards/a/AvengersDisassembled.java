package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "124")
public class AvengersDisassembled extends Card {

    public AvengersDisassembled() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Avengers Disassembled deals 3 damage to each creature",
                        new MassDamageEffect(3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target land. Its controller may search their library for a basic land card, "
                                + "put it onto the battlefield tapped, then shuffle",
                        new DestroyTargetPermanentThenEffect(
                                new SearchLibraryEffect(
                                        CardPredicateUtils.basicLand(),
                                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                                ThenEffectRecipient.TARGET_CONTROLLER),
                        TargetFilters.land())
        )));
    }
}
