package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsWithDifferentPowersToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "228")
public class RipSpawnHunter extends Card {

    public RipSpawnHunter() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED,
                new SurvivalTriggerEffect(new ConditionalEffect(
                        new SourceIsTapped(),
                        new RevealTopCardsWithDifferentPowersToHandEffect(
                                new SourcePower(),
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardSubtypePredicate(CardSubtype.VEHICLE)))))));
    }
}
