package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "EMA", collectorNumber = "127")
@CardRegistration(set = "2XM", collectorNumber = "124")
@CardRegistration(set = "SPG", collectorNumber = "153")
@CardRegistration(set = "C14", collectorNumber = "34")
@CardRegistration(set = "C21", collectorNumber = "165")
@CardRegistration(set = "C20", collectorNumber = "150")
@CardRegistration(set = "CM2", collectorNumber = "94")
@CardRegistration(set = "E01", collectorNumber = "46")
public class DualcasterMage extends Card {

    public DualcasterMage() {
        // When Dualcaster Mage enters, copy target instant or sorcery spell.
        // You may choose new targets for the copy.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CopySpellEffect(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL))
        ));
    }
}
