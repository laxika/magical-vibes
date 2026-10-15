package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeToOwnerOfTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsCardExiledWithSourcePredicate;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "139")
public class TheRuinousPowers extends Card {

    public TheRuinousPowers() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new LoseLifeToOwnerOfTriggeringSpellEffect()),
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryCastFromZonePredicate(Zone.EXILE),
                        new StackEntryIsCardExiledWithSourcePredicate()))));
    }
}
