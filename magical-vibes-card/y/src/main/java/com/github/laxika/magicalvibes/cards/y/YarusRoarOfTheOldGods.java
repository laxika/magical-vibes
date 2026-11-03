package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "245")
@CardRegistration(set = "MKM", collectorNumber = "375")
public class YarusRoarOfTheOldGods extends Card {

    public YarusRoarOfTheOldGods() {
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsFaceDownPredicate()),
                TriggerOptions.ONE_OR_MORE,
                new DrawCardEffect(1));
        addTrigger(Triggers.creatureDies(new PermanentIsFaceDownPredicate(), ObjectRelation.YOU_CONTROL),
                new ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect());
    }
}
