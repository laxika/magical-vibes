package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "91")
public class InsatiableAvarice extends Card {

    public InsatiableAvarice() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{2}", "{B}{B}")));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a card, then shuffle and put that card on top",
                        new SearchLibraryEffect(null, LibrarySearchDestination.TOP_OF_LIBRARY)),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player draws three cards and loses 3 life",
                        List.of(
                                new DrawCardForTargetPlayerEffect(3, false, true),
                                new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER)),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player."))
        )));
    }
}
