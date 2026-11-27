package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "152")
@CardRegistration(set = "DBL", collectorNumber = "419")
public class CurseOfHospitality extends Card {

    public CurseOfHospitality() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.TRAMPLE,
                Subjects.otherCreatures(),
                new PermanentIsAttackingEnchantedPlayerPredicate()));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopCardMayPlayThisTurnEffect(1, false)
                        .fromLibraryOf(Subjects.triggeringPlayer())
                        .playableBy(Subjects.controllerOf(Subjects.triggering()))
                        .withManaAsThoughAnyColor());
    }
}
