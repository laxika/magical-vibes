package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "217")
public class RulikMonsWarrenChief extends Card {

    public RulikMonsWarrenChief() {
        addTrigger(Triggers.attacks(),
                new LookAtTopCardMayPutMatchingOntoBattlefieldEffect(new CardTypePredicate(CardType.LAND), true),
                new ConditionalEffect(new NotCondition(HappenedThisWay.returned()),
                        new CreateTokenEffect("Goblin", 1, 1, CardColor.RED,
                                List.of(CardSubtype.GOBLIN), Set.of(), Set.of())));
    }
}
