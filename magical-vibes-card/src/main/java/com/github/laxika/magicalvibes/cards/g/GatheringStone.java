package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "257")
public class GatheringStone extends Card {

    public GatheringStone() {
        CardHasSourceChosenSubtypePredicate chosenType = new CardHasSourceChosenSubtypePredicate();

        // As this artifact enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // Spells you cast of the chosen type cost {1} less to cast.
        addEffect(EffectSlot.STATIC,
                new ReduceCastCostForMatchingSpellsEffect(chosenType, 1, CostModificationScope.SELF));

        // When this artifact enters and at the beginning of your upkeep, look at the top card of
        // your library. If it is a card of the chosen type, you may reveal it and put it into your
        // hand. If you don't, you may put it into your graveyard.
        LookAtTopCardMayRevealMatchingToHandEffect topCard =
                new LookAtTopCardMayRevealMatchingToHandEffect(chosenType, true);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, topCard);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, topCard);
    }
}
