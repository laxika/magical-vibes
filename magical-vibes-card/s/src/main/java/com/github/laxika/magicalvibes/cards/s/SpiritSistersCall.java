package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCardTypeWithTargetCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "237")
@CardRegistration(set = "DSC", collectorNumber = "232")
public class SpiritSistersCall extends Card {

    public SpiritSistersCall() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentSharesCardTypeWithTargetCardPredicate(), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsPermanentPredicate())
                                .targetGraveyard(true)
                                .exileIfLeavesBattlefield(true)
                                .build())),
                "Sacrifice a permanent that shares a card type with the chosen card?"));
    }
}
