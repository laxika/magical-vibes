package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "132")
public class EscapeVelocity extends Card {

    public EscapeVelocity() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 0, Set.of(Keyword.HASTE), Subjects.attachedCreature()));

        addCastingOption(new GraveyardCast(null, "{1}{R}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 2)),
                null, false, false, true));
    }
}
