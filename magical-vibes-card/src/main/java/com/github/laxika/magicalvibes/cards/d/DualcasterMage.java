package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "EMA", collectorNumber = "127")
@CardRegistration(set = "2XM", collectorNumber = "124")
public class DualcasterMage extends Card {

    public DualcasterMage() {
        // When Dualcaster Mage enters, copy target instant or sorcery spell.
        // You may choose new targets for the copy.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopySpellEffect(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL))
        ));
    }
}
