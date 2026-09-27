package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "USG", collectorNumber = "100")
@CardRegistration(set = "VMA", collectorNumber = "95")
@CardRegistration(set = "DMR", collectorNumber = "67")
@CardRegistration(set = "SOC", collectorNumber = "203")
@CardRegistration(set = "C14", collectorNumber = "129")
@CardRegistration(set = "C15", collectorNumber = "108")
public class StrokeOfGenius extends Card {

    public StrokeOfGenius() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL, new DrawCardForTargetPlayerEffect(new XValue(), false, true));
    }
}
