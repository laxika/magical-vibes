package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "57")
public class CurseOfThirst extends Card {

    public CurseOfThirst() {
        // At the beginning of enchanted player's upkeep, this Aura deals damage to that player
        // equal to the number of Curses attached to them: counted with that player as the controller,
        // so "attached to the source's controller" is attached to that player.
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),
                DealDamageEffect.eachEvaluatedFor(new PermanentCount(new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.CURSE),
                                new PermanentAttachedToSourceControllerPredicate())), CountScope.ANY_PLAYER),
                        Subjects.triggeringPlayer()));
    }
}
