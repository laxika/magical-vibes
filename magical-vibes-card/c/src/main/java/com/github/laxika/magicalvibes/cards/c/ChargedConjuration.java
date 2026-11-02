package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceInstantAndSorceryCastCostInHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBLB", collectorNumber = "14")
public class ChargedConjuration extends Card {

    public ChargedConjuration() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new PerpetuallyReduceInstantAndSorceryCastCostInHandEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new ConjureCardFromSpellbookToHandEffect(List.of(
                                new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("TSP", "152"),
                                new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("MH2", "127"),
                                new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("TSP", "160")
                        ))
                ),
                "Sacrifice this enchantment: Conjure a card of your choice from Charged Conjuration's spellbook into your hand. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
