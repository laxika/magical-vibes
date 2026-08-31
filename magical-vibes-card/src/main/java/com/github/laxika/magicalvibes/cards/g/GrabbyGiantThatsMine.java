package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.ThatsMine;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "133")
public class GrabbyGiantThatsMine extends Card {

    public GrabbyGiantThatsMine() {
        setBackFaceCard(new ThatsMine());
        addCastingOption(new AdventureCast("{1}{R}"));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsLandPredicate()
                                )),
                                "an artifact or land"),
                        new DrawCardEffect(1)
                ),
                "{2}{R}, {T}, Sacrifice an artifact or land: Draw a card."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ThatsMine";
    }
}
