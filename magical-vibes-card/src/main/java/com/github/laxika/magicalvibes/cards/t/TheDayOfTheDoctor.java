package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseUpToNMatchingCreaturesThenMayExileRestEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUntilCardPredicateMayPlayWhileSourceControlledEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "WHO", collectorNumber = "121")
public class TheDayOfTheDoctor extends Card {

    public TheDayOfTheDoctor() {
        ExileUntilCardPredicateMayPlayWhileSourceControlledEffect search =
                new ExileUntilCardPredicateMayPlayWhileSourceControlledEffect(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY));
        addEffect(EffectSlot.SAGA_CHAPTER_I, search);
        addEffect(EffectSlot.SAGA_CHAPTER_II, search);
        addEffect(EffectSlot.SAGA_CHAPTER_III, search);
        addEffect(EffectSlot.SAGA_CHAPTER_IV,
                new ChooseUpToNMatchingCreaturesThenMayExileRestEffect(
                        3, new PermanentHasSubtypePredicate(CardSubtype.DOCTOR), "Doctors"));
    }
}
