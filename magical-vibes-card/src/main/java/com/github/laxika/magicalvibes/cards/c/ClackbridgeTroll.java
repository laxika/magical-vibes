package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "84")
public class ClackbridgeTroll extends Card {

    public ClackbridgeTroll() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect(3, "Goat", 0, 1, CardColor.WHITE,
                        List.of(CardSubtype.GOAT), Set.of(), Set.of())
        ));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect());
    }
}
