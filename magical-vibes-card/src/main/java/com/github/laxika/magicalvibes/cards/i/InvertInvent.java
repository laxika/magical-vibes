package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "228")
public class InvertInvent extends Card {

    public InvertInvent() {
        TargetFilter creature = TargetFilters.creature();
        CardEffect invert = new SwitchPowerToughnessEffect();
        CardEffect searchInstant = new SearchLibraryEffect(
                new Fixed(1),
                new CardTypePredicate(CardType.INSTANT),
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
                false);
        CardEffect searchSorcery = new SearchLibraryEffect(new CardTypePredicate(CardType.SORCERY));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Invert — Switch the power and toughness of each of up to two target creatures until end of turn",
                        List.of(invert),
                        creature,
                        null,
                        0,
                        2,
                        false,
                        "{U/R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Invent — Search your library for an instant card and/or a sorcery card, reveal them, put them into your hand, then shuffle",
                        List.of(searchInstant, searchSorcery)
                ).withManaCost("{4}{U}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Invert and then Invent",
                        List.of(invert, searchInstant, searchSorcery),
                        creature,
                        null,
                        0,
                        2,
                        false,
                        "{4}{U}{R}{U/R}")
        )));
    }
}
