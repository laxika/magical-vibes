package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "603")
public class NickFurySpymaster extends Card {

    private static final CardAllOfPredicate CREATURE_MANA_VALUE_THREE_OR_LESS = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.CREATURE),
            new CardMaxManaValuePredicate(3)));

    public NickFurySpymaster() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new ConditionalEffect(new AttacksAlone(),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new MayEffect(
                                new PutCardToBattlefieldThenEffect(
                                        CREATURE_MANA_VALUE_THREE_OR_LESS,
                                        "creature",
                                        true,
                                        true,
                                        null,
                                        new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(
                                                Keyword.INDESTRUCTIBLE, null)),
                                "Put a creature card with mana value 3 or less from your hand onto the battlefield tapped and attacking?"))));
    }
}
