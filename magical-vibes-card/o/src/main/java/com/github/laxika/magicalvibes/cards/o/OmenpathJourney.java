package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BIG", collectorNumber = "18")
public class OmenpathJourney extends Card {

    public OmenpathJourney() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryForCardsToExileWithSourceEffect(
                        new CardTypePredicate(CardType.LAND), 5, true));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ReturnCardExiledWithSourceToBattlefieldEffect(
                        null, false, null, true, false, true));
    }
}
