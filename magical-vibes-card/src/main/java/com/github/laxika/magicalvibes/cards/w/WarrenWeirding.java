package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "82")
public class WarrenWeirding extends Card {

    public WarrenWeirding() {
        // Two 1/1 black Goblin Rogue tokens with haste until end of turn, created by the sacrificing player.
        CreateTokenEffect goblinRogues = new CreateTokenEffect(
                2, "Goblin Rogue", 1, 1, CardColor.BLACK, null,
                List.of(CardSubtype.GOBLIN, CardSubtype.ROGUE),
                Set.of(), Set.of(Keyword.HASTE));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ))
                .addEffect(EffectSlot.SPELL,
                        new TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffect(
                                CardSubtype.GOBLIN, goblinRogues));
    }
}
