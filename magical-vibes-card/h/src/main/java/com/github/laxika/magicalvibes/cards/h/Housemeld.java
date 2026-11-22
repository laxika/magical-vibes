package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBecomeOnlyCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDSK", collectorNumber = "5")
public class Housemeld extends Card {

    public Housemeld() {
        target(TargetFilters.creature())
                .addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(new PerpetuallyBecomeOnlyCardTypeEffect(
                        Subjects.cardsThisWay(StepKind.EXILE), CardType.ENCHANTMENT))
                .addSpellEffect(RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.YOU),
                        ReturnAllCardsExiledWithSourceEffect.putOntoBattlefield(Subjects.cardsThisWay(StepKind.EXILE))));
    }
}
