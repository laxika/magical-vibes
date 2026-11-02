package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMC", collectorNumber = "36")
@CardRegistration(set = "DMC", collectorNumber = "58")
public class OhabiCaleria extends Card {

    public OhabiCaleria() {
        // Untap all Archers you control during each other player's untap step.
        addStaticEffect(new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(
                TurnStep.UNTAP, new PermanentHasSubtypePredicate(CardSubtype.ARCHER),
                TapUntapScope.CONTROLLED));

        // Whenever an Archer you control deals damage to a creature, you may pay {2}. If you do,
        // draw a card.
        addTrigger(Triggers.creatureYouControlDealsDamageToCreature(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.ARCHER),
                        new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay {2} to draw a card?")));
    }
}
