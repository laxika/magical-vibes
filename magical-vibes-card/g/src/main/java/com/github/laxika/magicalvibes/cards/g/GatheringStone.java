package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "257")
public class GatheringStone extends Card {

    public GatheringStone() {
        CardHasSourceChosenSubtypePredicate chosenType = new CardHasSourceChosenSubtypePredicate(false);

        // As this artifact enters, choose a creature type.
        addStaticEffect(new ChooseSubtypeOnEnterEffect());

        // Spells you cast of the chosen type cost {1} less to cast.
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(chosenType, 1, CostModificationScope.SELF));

        // When this artifact enters and at the beginning of your upkeep, look at the top card of
        // your library. If it is a card of the chosen type, you may reveal it and put it into your
        // hand. If you don't, you may put it into your graveyard.
        LookAtTopCardMayRevealMatchingToHandEffect topCard =
                new LookAtTopCardMayRevealMatchingToHandEffect(chosenType, true);
        addTrigger(Triggers.enters(ObjectRelation.THIS), topCard);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), topCard);
    }
}
