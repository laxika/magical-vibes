package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardOneThenApplyEffectsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "89")
@CardRegistration(set = "FIC", collectorNumber = "179")
@CardRegistration(set = "FIC", collectorNumber = "476")
public class MogMoogleWarrior extends Card {

    public MogMoogleWarrior() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new EachPlayerMayDiscardOneThenApplyEffectsEffect(
                        new CreateTokenEffect("Moogle", 1, 2, CardColor.WHITE,
                                List.of(CardSubtype.MOOGLE), Set.of(Keyword.LIFELINK), Set.of()),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1,
                                new PermanentHasSubtypePredicate(CardSubtype.MOOGLE))));
    }
}
