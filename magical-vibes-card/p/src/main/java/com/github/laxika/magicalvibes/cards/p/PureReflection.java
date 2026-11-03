package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "27")
public class PureReflection extends Card {

    public PureReflection() {
        CreateTokenEffect reflectionToken = new CreateTokenEffect(
                "Reflection", new EventValue(), new EventValue(), CardColor.WHITE,
                List.of(CardSubtype.REFLECTION), Set.of(), Set.of());

        addTrigger(Triggers.playerCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new DestroyAllPermanentsEffect(new PermanentHasSubtypePredicate(CardSubtype.REFLECTION)),
                new CreateTokenForTriggeringPlayerEffect(reflectionToken));
    }
}
