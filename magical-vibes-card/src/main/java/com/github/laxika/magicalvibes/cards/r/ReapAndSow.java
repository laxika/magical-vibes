package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "81")
public class ReapAndSow extends Card {

    public ReapAndSow() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{1}{G}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target land",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.land()),
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a land card, put that card onto the battlefield, then shuffle",
                        new SearchLibraryEffect(new CardTypePredicate(CardType.LAND), LibrarySearchDestination.BATTLEFIELD))
        )));
    }
}
