package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "SLD", collectorNumber = "237")
@CardRegistration(set = "CMM", collectorNumber = "309")
@CardRegistration(set = "CMM", collectorNumber = "567")
@CardRegistration(set = "CMM", collectorNumber = "650")
@CardRegistration(set = "SOC", collectorNumber = "279")
public class OhranFrostfang extends Card {

    public OhranFrostfang() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.DEATHTOUCH,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, new DrawCardEffect(1)));
    }
}
