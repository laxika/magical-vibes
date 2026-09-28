package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "7")
public class QuintoriusHistoryChaser extends Card {

    public QuintoriusHistoryChaser() {
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD, new CreateTokenEffect(
                "Spirit", 3, 2, CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE),
                List.of(CardSubtype.SPIRIT)));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new MayEffect(
                        new DiscardCardThenEffect(
                                null,
                                SequenceEffect.of(
                                        new DrawCardEffect(2),
                                        new MillEffect(1, MillRecipient.CONTROLLER)),
                                "a card"),
                        "Discard a card?")),
                "+1: You may discard a card. If you do, draw two cards, then mill a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new GrantKeywordEffect(
                        Set.of(Keyword.DOUBLE_STRIKE, Keyword.VIGILANCE),
                        GrantScope.OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.SPIRIT))),
                "−4: Spirits you control gain double strike and vigilance until end of turn."
        ));
    }
}
