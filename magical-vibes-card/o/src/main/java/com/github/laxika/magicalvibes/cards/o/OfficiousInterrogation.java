package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MKM", collectorNumber = "222")
@CardRegistration(set = "MKM", collectorNumber = "314")
public class OfficiousInterrogation extends Card {

    public OfficiousInterrogation() {
        setAdditionalManaCostPerExtraTarget("{W}{U}");
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player."), 0, 99)
                .addSpellEffect(CreateTokenEffect.ofClueToken(
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.TARGET_PLAYERS)));
    }
}
