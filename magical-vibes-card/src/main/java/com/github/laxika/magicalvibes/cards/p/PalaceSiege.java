package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "79")
public class PalaceSiege extends Card {

    private static final String KHANS = "Khans";
    private static final String DRAGONS = "Dragons";

    public PalaceSiege() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseModeOnEnterEffect(List.of(KHANS, DRAGONS)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new SourceHasChosenMode(KHANS),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .build()));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new SourceHasChosenMode(DRAGONS),
                        SequenceEffect.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(2))));
    }
}
