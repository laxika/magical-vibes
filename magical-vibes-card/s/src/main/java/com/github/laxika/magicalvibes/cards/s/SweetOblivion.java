package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "THB", collectorNumber = "70")
public class SweetOblivion extends Card {

    public SweetOblivion() {
        addSpellEffect(new MillEffect(4, Subjects.targetPlayer()));
        addCastingOption(new GraveyardCast(null, "{3}{U}",
                List.of(new ExileNCardsFromGraveyardCastingCost(null, "other cards", 4)),
                null, false, false, true));
    }
}
