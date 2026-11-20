package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByWatchedPlayerPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GiveRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "138")
@CardRegistration(set = "PIP", collectorNumber = "434")
@CardRegistration(set = "PIP", collectorNumber = "666")
@CardRegistration(set = "PIP", collectorNumber = "962")
public class NukaNukeLauncher extends Card {

    public NukaNukeLauncher() {
        addStaticEffect(new StaticBoostEffect(3, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.INTIMIDATE, Subjects.attachedCreature()));
        addTrigger(Triggers.attacks(),
                RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect.untilWatchedPlayersNextTurn(
                        new TriggeredAbility(Triggers.playerCastsSpell()
                                .withStackEntryFilter(new StackEntryControlledByWatchedPlayerPredicate()),
                                new GiveRadCountersEffect(2, Subjects.targetPlayer()))));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
