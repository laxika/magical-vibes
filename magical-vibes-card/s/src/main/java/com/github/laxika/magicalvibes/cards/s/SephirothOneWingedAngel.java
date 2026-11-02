package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AsTransformsIntoThisEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class SephirothOneWingedAngel extends Card {

    public SephirothOneWingedAngel() {
        addStaticEffect(new AsTransformsIntoThisEffect(new CreateEmblemEffect(
                List.of(new EmblemCreatureDeathTriggerEffect(
                        List.of(SephirothFabledSoldier.DRAIN),
                        SephirothFabledSoldier.OPPONENT_TARGET)),
                "Whenever a creature dies, target opponent loses 1 life and you gain 1 life.")));
        addTrigger(Triggers.attacks(), new SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())))));
    }
}
