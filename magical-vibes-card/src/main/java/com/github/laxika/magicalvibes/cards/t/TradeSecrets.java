package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawUpToNCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TradeSecretsRepeatEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ONS", collectorNumber = "118")
public class TradeSecrets extends Card {

    public TradeSecrets() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));
        addEffect(EffectSlot.SPELL, new DrawCardForTargetPlayerEffect(2, false, true));
        addEffect(EffectSlot.SPELL, new DrawUpToNCardsEffect(4));
        addEffect(EffectSlot.SPELL, new TradeSecretsRepeatEffect());
    }
}
