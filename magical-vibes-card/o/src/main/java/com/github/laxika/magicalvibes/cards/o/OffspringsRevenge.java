package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

import static com.github.laxika.magicalvibes.model.Keyword.HASTE;

@CardRegistration(set = "IKO", collectorNumber = "198")
public class OffspringsRevenge extends Card {

    public OffspringsRevenge() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), SequenceEffect.of(
                new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardAnyOfPredicate(List.of(
                                        new CardColorPredicate(CardColor.RED),
                                        new CardColorPredicate(CardColor.WHITE),
                                        new CardColorPredicate(CardColor.BLACK))))),
                        true,
                        List.of(),
                        false,
                        false,
                        null,
                        1,
                        1),
                new GrantKeywordEffect(HASTE, GrantScope.TOKENS_CREATED_THIS_RESOLUTION,
                        GrantDuration.UNTIL_YOUR_NEXT_TURN)));
    }
}
