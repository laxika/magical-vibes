package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "73")
public class ReflectionsOfLittjara extends Card {

    public ReflectionsOfLittjara() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CopyControllerCastSpellOnSpellCastEffect(
                new CardHasSourceChosenSubtypePredicate(false),
                null,
                null,
                null,
                null,
                Set.of(),
                null,
                Set.of(),
                true,
                true
        ));
    }
}
