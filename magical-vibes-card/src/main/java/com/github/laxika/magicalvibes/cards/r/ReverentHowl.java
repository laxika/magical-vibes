package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "81")
public class ReverentHowl extends Card {

    public ReverentHowl() {
        var targetPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player draws two cards and loses 2 life",
                        List.of(
                                new DrawCardForTargetPlayerEffect(2, false, true),
                                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER)),
                        targetPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +2/+2 and gains lifelink until end of turn",
                        List.of(
                                new BoostTargetCreatureEffect(2, 2),
                                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET)),
                        TargetFilters.creature())
        )));
    }
}
