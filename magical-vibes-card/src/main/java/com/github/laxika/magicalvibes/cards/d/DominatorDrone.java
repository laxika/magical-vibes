package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDefendingPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "92")
public class DominatorDrone extends Card {

    public DominatorDrone() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsAnotherPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsColorlessPredicate(),
                        new PermanentIsCreaturePredicate()))),
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopCardsOfDefendingPlayerLibraryEffect(1));
    }
}
