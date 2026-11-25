package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "33")
public class Apathy extends Card {

    public Apathy() {
        // "Enchant creature. Enchanted creature doesn't untap during its controller's untap step."
        target(TargetFilters.creature()).addStaticEffect(DoesntUntapEffect.enchanted());

        // "At the beginning of the upkeep of enchanted creature's controller, that player may
        // discard a card at random. If the player does, untap that creature."
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                UnlessPaysEffect.mayPay(Subjects.activePlayer(), new DiscardRandomCardCost(1),
                        new UntapPermanentsEffect(Subjects.attached()))
                        .withPrompt("Discard a card at random to untap the enchanted creature?"));
    }
}
