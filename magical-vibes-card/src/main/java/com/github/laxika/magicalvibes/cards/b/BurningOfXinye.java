package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyRecipient;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerDestroysPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "PTK", collectorNumber = "104")
public class BurningOfXinye extends Card {

    public BurningOfXinye() {
        // You destroy four lands you control...
        addEffect(EffectSlot.SPELL, new PlayerDestroysPermanentsEffect(
                4, new PermanentIsLandPredicate(), DestroyRecipient.CONTROLLER));
        // ...then target opponent destroys four lands they control...
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new PlayerDestroysPermanentsEffect(
                4, new PermanentIsLandPredicate(), DestroyRecipient.TARGET_PLAYER));
        // ...then Burning of Xinye deals 4 damage to each creature.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(4));
    }
}
