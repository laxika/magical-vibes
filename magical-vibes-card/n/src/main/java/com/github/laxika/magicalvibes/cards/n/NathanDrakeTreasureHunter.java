package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpendAnyManaTypeToCastEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorForActivatedAbilitiesOfNonOwnedPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2216")
public class NathanDrakeTreasureHunter extends Card {

    public NathanDrakeTreasureHunter() {
        addStaticEffect(SpendAnyManaTypeToCastEffect.spells(new CardAllOfPredicate(List.of(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                new CardControllerDoesNotOwnPredicate()))));
        addStaticEffect(new SpendManaAsAnyColorForActivatedAbilitiesOfNonOwnedPermanentsEffect());
        addTrigger(Triggers.attacks(), new ExileTopCardOfEachPlayersLibraryAndMayCastSpellsEffect());
    }
}
