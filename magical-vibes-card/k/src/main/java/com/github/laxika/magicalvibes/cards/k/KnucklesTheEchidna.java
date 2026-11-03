package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "984")
@CardRegistration(set = "SLD", collectorNumber = "2082")
public class KnucklesTheEchidna extends Card {

    public KnucklesTheEchidna() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                TriggerOptions.ONE_OR_MORE,
                CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControlsPermanentCount(30, new PermanentIsArtifactPredicate())),
                new WinGameEffect());
    }
}
