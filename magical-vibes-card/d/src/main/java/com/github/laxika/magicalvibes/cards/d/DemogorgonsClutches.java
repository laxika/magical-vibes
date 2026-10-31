package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "AFR", collectorNumber = "96")
@CardRegistration(set = "HBG", collectorNumber = "151")
public class DemogorgonsClutches extends Card {

    public DemogorgonsClutches() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent."
        ))
                .addSpellEffect(new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER))
                .addSpellEffect(new MillEffect(2, MillRecipient.TARGET_PLAYER))
                .addSpellEffect(new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));
    }
}
