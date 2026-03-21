package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "59")
public class NaruMehaMasterWizard extends Card {

    public NaruMehaMasterWizard() {
        // Flash — auto-loaded from Scryfall

        // When Naru Meha, Master Wizard enters, copy target instant or sorcery spell you control.
        // You may choose new targets for the copy.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopySpellEffect(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        new StackEntryControlledByPredicate()
                ))
        ));

        // Other Wizards you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1,
                GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WIZARD))
        ));
    }
}
