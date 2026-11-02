package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "234")
public class MirrorShield extends Card {

    public MirrorShield() {
        addStaticEffect(new StaticBoostEffect(0, 2, Set.of(Keyword.HEXPROOF), GrantScope.EQUIPPED_CREATURE));

        CardEffect destroyDeathtouchCreature = new TriggeringPermanentConditionalEffect(
                new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH),
                new DestroyCombatOpponentEffect(false), false, true);
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.blocks(), destroyDeathtouchCreature), GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesBlocked(),
                        destroyDeathtouchCreature), GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
