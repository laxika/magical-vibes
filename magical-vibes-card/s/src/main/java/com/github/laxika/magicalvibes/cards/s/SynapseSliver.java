package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "53")
@CardRegistration(set = "SLD", collectorNumber = "626")
public class SynapseSliver extends Card {

    public SynapseSliver() {
        addTrigger(Triggers.creatureDealsCombatDamageToPlayer(), new TriggeringPermanentConditionalEffect(
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER),
                new MayEffect(new DrawCardForTriggeringPermanentControllerEffect(), "Draw a card?", null,
                        MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER)));
    }
}
