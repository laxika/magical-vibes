package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTriggeringAttackersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MAT", collectorNumber = "48")
@CardRegistration(set = "MAT", collectorNumber = "98")
@CardRegistration(set = "MAT", collectorNumber = "148")
@CardRegistration(set = "MAT", collectorNumber = "183")
@CardRegistration(set = "MAT", collectorNumber = "227")
public class TyvarTheBellicose extends Card {

    public TyvarTheBellicose() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new GrantKeywordToTriggeringAttackersEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ELF), Keyword.DEATHTOUCH));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_SELF_MANA_ABILITY_RESOLVES,
                OncePerTurnTriggerEffect.perGrant(new PutCountersOnSelfEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, new EventValue())),
                GrantScope.ALL_OWN_CREATURES));
    }
}
