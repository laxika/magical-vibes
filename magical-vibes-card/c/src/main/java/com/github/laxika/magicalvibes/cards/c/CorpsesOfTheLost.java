package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.DescendedThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "98")
@CardRegistration(set = "LCI", collectorNumber = "366")
public class CorpsesOfTheLost extends Card {

    public CorpsesOfTheLost() {
        addStaticEffect(new StaticBoostEffect(1, 0, Set.of(Keyword.HASTE), GrantScope.ALL_OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.SKELETON)));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect("Skeleton Pirate", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.SKELETON, CardSubtype.PIRATE), Set.of(), Set.of()));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new DescendedThisTurn()),
                new MayPayLifeEffect(
                        1,
                        ReturnToHandEffect.self(),
                        "Pay 1 life to return Corpses of the Lost to its owner's hand?"));
    }
}
