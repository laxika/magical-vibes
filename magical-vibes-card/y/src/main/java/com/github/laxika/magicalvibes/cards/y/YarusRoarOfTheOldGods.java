package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "245")
@CardRegistration(set = "MKM", collectorNumber = "375")
public class YarusRoarOfTheOldGods extends Card {

    public YarusRoarOfTheOldGods() {
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), new AllyCombatDamageTriggerEffect(
                new PermanentIsFaceDownPredicate(), new DrawCardEffect(1), false, true));
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), new TriggeringPermanentConditionalEffect(
                new PermanentIsFaceDownPredicate(),
                new ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect()));
    }
}
