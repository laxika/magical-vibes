package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "124")
public class AvengersDisassembled extends Card {

    public AvengersDisassembled() {
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Avengers Disassembled deals 3 damage to each creature",
                        new MassDamageEffect(3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target land. Its controller may search their library for a basic land card, "
                                + "put it onto the battlefield tapped, then shuffle",
                        SequenceEffect.of(
                                new DestroyTargetPermanentEffect(),
                                new MayEffect(new SearchLibraryEffect(
                                        CardPredicateUtils.basicLand(),
                                        LibrarySearchDestination.BATTLEFIELD_TAPPED).searchedBy(Subjects.controllerOf(Subjects.target())),
                                        "Search your library for a basic land card?", null, MayChoicePlayer.TARGET_PERMANENT_CONTROLLER)),
                        TargetFilters.land())
        )));
    }
}
