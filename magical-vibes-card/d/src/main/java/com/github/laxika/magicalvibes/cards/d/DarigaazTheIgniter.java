package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasChosenSpellColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "243")
@CardRegistration(set = "DDE", collectorNumber = "47")
public class DarigaazTheIgniter extends Card {

    public DarigaazTheIgniter() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                UnlessPaysEffect.youMayPay("{2}{R}", SequenceEffect.of(
                                new ChooseColorAtResolutionEffect(),
                                RevealCardsFromHandEffect.hand(Subjects.triggeringPlayer()),
                                DealDamageEffect.to(Subjects.triggeringPlayer(),
                                        AmountThisWay.revealedCards(new CardHasChosenSpellColorPredicate())),
                                new ClearChosenColorEffect())));
    }
}
