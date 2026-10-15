package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GreatIntelligencesPlanVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "WHO", collectorNumber = "133")
@CardRegistration(set = "WHO", collectorNumber = "738")
public class GreatIntelligencesPlan extends Card {

    public GreatIntelligencesPlan() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.SPELL, new GreatIntelligencesPlanVillainousChoiceEffect());
    }
}
