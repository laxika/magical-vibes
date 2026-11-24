package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpendAnyManaTypeToCastEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorForActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2216")
public class NathanDrakeTreasureHunter extends Card {

    public NathanDrakeTreasureHunter() {
        addStaticEffect(SpendAnyManaTypeToCastEffect.asThoughAnyColor(new CardAllOfPredicate(List.of(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                new CardControllerDoesNotOwnPredicate()))));
        addStaticEffect(new SpendManaAsAnyColorForActivatedAbilitiesEffect(Subjects.controlledBy(Subjects.you(),
                new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate()))));
        addTrigger(Triggers.attacks(), new ExileTopCardOfEachPlayersLibraryAndMayCastSpellsEffect());
    }
}
