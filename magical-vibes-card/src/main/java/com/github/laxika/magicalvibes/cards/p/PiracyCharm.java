package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "58")
public class PiracyCharm extends Card {

    public PiracyCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gains islandwalk until end of turn",
                        new GrantKeywordEffect(Keyword.ISLANDWALK, GrantScope.TARGET),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +2/-1 until end of turn",
                        new BoostTargetCreatureEffect(2, -1),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player discards a card",
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player"))
        )));
    }
}
