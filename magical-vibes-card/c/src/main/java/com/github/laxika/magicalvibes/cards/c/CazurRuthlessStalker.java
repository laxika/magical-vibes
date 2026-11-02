package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "188")
@CardRegistration(set = "C20", collectorNumber = "5")
public class CazurRuthlessStalker extends Card {

    private static final String PARTNER_NAME = "Ukkima, Stalking Shadow";

    public CazurRuthlessStalker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put " + PARTNER_NAME + " into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));

        // Whenever a creature you control deals combat damage to a player, put a +1/+1 counter on that creature.
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        new PutCountersOnSourceEffect(1, 1, 1),
                        true));
    }
}
