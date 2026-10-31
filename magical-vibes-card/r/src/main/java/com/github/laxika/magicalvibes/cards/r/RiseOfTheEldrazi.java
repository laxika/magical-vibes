package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CMM", collectorNumber = "716")
@CardRegistration(set = "CMM", collectorNumber = "749")
public class RiseOfTheEldrazi extends Card {

    public RiseOfTheEldrazi() {
        addStaticEffect(new CantBeCounteredEffect());
        target(TargetFilters.permanent())
                .addSpellEffect(new DestroyTargetPermanentEffect());
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addSpellEffect(new DrawCardForTargetPlayerEffect(4, false, true));
        addSpellEffect(new ControllerExtraTurnEffect(1));
        addSpellEffect(new ExileSpellEffect());
    }
}
