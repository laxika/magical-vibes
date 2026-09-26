package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "36")
@CardRegistration(set = "DDH", collectorNumber = "65")
@CardRegistration(set = "EMA", collectorNumber = "45")
@CardRegistration(set = "VMA", collectorNumber = "65")
@CardRegistration(set = "DMR", collectorNumber = "46")
@CardRegistration(set = "2X2", collectorNumber = "44")
@CardRegistration(set = "C13", collectorNumber = "38")
@CardRegistration(set = "CMM", collectorNumber = "86")
@CardRegistration(set = "LTC", collectorNumber = "188")
public class DeepAnalysis extends Card {

    public DeepAnalysis() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL, new DrawCardForTargetPlayerEffect(2, false, true));
        addCastingOption(new FlashbackCast(List.of(
                new ManaCastingCost("{1}{U}"),
                new LifeCastingCost(3)
        )));
    }
}
