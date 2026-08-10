package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "96")
public class InciteWar extends Card {

    public InciteWar() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}"));

        var playerFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures target player controls attack this turn if able",
                        new TargetPlayersCreaturesMustAttackThisTurnEffect(), playerFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control gain first strike until end of turn",
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES))
        )));
    }
}
