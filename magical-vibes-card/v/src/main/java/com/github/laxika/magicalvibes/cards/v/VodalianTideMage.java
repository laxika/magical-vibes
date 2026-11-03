package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "29")
public class VodalianTideMage extends Card {

    public VodalianTideMage() {
        PermanentPredicate otherNontokenCreature = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(otherNontokenCreature),
                TriggerOptions.ONE_OR_MORE,
                new ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect());
    }
}
