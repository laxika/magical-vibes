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
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "234")
public class MirrorShield extends Card {

    public MirrorShield() {
        addStaticEffect(new StaticBoostEffect(0, 2, Set.of(Keyword.HEXPROOF), GrantScope.EQUIPPED_CREATURE));

        // "Whenever a creature with deathtouch blocks or becomes blocked by this creature": the filter is on the
        // creature on the other side of the block.
        PermanentHasKeywordPredicate deathtouch = new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH);
        CardEffect destroyDeathtouchCreature = new DestroyCombatOpponentEffect(false);
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.blocks().withFilter(deathtouch), destroyDeathtouchCreature),
                GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesBlocked().withFilter(deathtouch), destroyDeathtouchCreature),
                GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
