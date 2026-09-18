package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "217")
public class RulikMonsWarrenChief extends Card {

    public RulikMonsWarrenChief() {
        addEffect(EffectSlot.ON_ATTACK,
                new LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect(
                        new CardTypePredicate(CardType.LAND),
                        true,
                        new CreateTokenEffect("Goblin", 1, 1, CardColor.RED,
                                List.of(CardSubtype.GOBLIN), Set.of(), Set.of())));
    }
}
