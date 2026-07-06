package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "XLN", collectorNumber = "91")
public class BishopOfTheBloodstained extends Card {

    public BishopOfTheBloodstained() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        // "target player loses 1 life for each Vampire you control" — the controller's
                        // Vampires (including Bishop itself), counted as the ability resolves.
                        new LoseLifeEffect(new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                                CountScope.CONTROLLER), LoseLifeRecipient.TARGET_PLAYER));
    }
}
