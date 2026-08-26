package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.DescendedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "98")
@CardRegistration(set = "LCI", collectorNumber = "366")
public class CorpsesOfTheLost extends Card {

    public CorpsesOfTheLost() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, Set.of(Keyword.HASTE), GrantScope.OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.SKELETON)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Skeleton Pirate", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.SKELETON, CardSubtype.PIRATE), Set.of(), Set.of()));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new DescendedThisTurn(), new MayPayLifeEffect(
                        1,
                        ReturnToHandEffect.self(),
                        "Pay 1 life to return Corpses of the Lost to its owner's hand?")));
    }
}
