package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillTwoThenSacrificeSelfIfMilledCardsShareAllTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "19")
@CardRegistration(set = "DSC", collectorNumber = "49")
public class DemonicCovenant extends Card {

    public DemonicCovenant() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                new ConditionalEffect(
                        new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.DEMON)),
                        SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                SequenceEffect.of(
                        new CreateTokenEffect("Demon", 5, 5, CardColor.BLACK,
                                List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), Set.of()),
                        new MillTwoThenSacrificeSelfIfMilledCardsShareAllTypesEffect()));
    }
}
