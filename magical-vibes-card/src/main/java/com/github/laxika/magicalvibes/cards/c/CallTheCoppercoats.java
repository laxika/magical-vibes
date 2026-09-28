package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachTargetPlayerCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "195")
@CardRegistration(set = "C20", collectorNumber = "23")
public class CallTheCoppercoats extends Card {

    public CallTheCoppercoats() {
        // Strive — This spell costs {1}{W} more to cast for each target beyond the first.
        setAdditionalManaCostPerExtraTarget("{1}{W}");

        // Choose any number of target opponents. Create X 1/1 white Human Soldier creature tokens,
        // where X is the number of creatures those opponents control.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"), 0, 99)
                .addEffect(EffectSlot.SPELL, new CreateTokensForEachTargetPlayerCreatureEffect(
                        new CreateTokenEffect("Human Soldier", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of())));
    }
}
