package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesMissyVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "8")
@CardRegistration(set = "WHO", collectorNumber = "1022")
@CardRegistration(set = "WHO", collectorNumber = "431")
@CardRegistration(set = "WHO", collectorNumber = "546")
@CardRegistration(set = "WHO", collectorNumber = "613")
@CardRegistration(set = "WHO", collectorNumber = "1137")
public class Missy extends Card {

    public Missy() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardNotPredicate(new CardTypePredicate(CardType.ARTIFACT)))),
                new ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect()));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new EachOpponentFacesMissyVillainousChoiceEffect());
    }
}
