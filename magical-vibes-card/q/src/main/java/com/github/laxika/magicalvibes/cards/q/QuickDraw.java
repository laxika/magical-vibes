package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "138")
public class QuickDraw extends Card {

    public QuickDraw() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new BoostTargetCreatureEffect(1, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent."
        ))
                .addSpellEffect(new RemoveKeywordEffect(Keyword.FIRST_STRIKE,
                        GrantScope.TARGET_PLAYERS_CREATURES))
                .addSpellEffect(new RemoveKeywordEffect(Keyword.DOUBLE_STRIKE,
                        GrantScope.TARGET_PLAYERS_CREATURES));
    }
}
