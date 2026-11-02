package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.ChooseUpToNMatchingCreaturesThenMayExileRestEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUntilCardPredicateMayPlayWhileSourceControlledEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "121")
@CardRegistration(set = "WHO", collectorNumber = "726")
public class TheDayOfTheDoctor extends Card {

    public TheDayOfTheDoctor() {
        ExileUntilCardPredicateMayPlayWhileSourceControlledEffect search =
                new ExileUntilCardPredicateMayPlayWhileSourceControlledEffect(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY));
        addTrigger(Triggers.chapter(1), search);
        addTrigger(Triggers.chapter(2), search);
        addTrigger(Triggers.chapter(3), search);
        addTrigger(Triggers.chapter(4),
                new ChooseUpToNMatchingCreaturesThenMayExileRestEffect(
                        3, new PermanentHasSubtypePredicate(CardSubtype.DOCTOR), "Doctors"));
    }
}
