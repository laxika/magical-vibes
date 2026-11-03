package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttackingPlayerIsOpponent;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C17", collectorNumber = "30")
@CardRegistration(set = "SCD", collectorNumber = "178")
public class CurseOfBounty extends Card {

    public CurseOfBounty() {
        PermanentNotPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksEnchantedPlayer()),
                SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, nonland),
                        new ConditionalEffect(
                                new AllOf(java.util.List.of(
                                        new AttackingPlayerIsOpponent(), new AttacksEnchantedPlayer())),
                                new UntapPermanentsEffect(
                                        TapUntapScope.TARGET_PLAYERS_PERMANENTS, nonland))));
    }
}
