package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.laxika.magicalvibes.model.Keyword.HASTE;

@CardRegistration(set = "IKO", collectorNumber = "198")
public class OffspringsRevenge extends Card {

    public OffspringsRevenge() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), SequenceEffect.of(
                ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.RED),
                                new CardColorPredicate(CardColor.WHITE),
                                new CardColorPredicate(CardColor.BLACK)))))),
                CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                        new CreateTokenCopyOfTargetPermanentEffect(List.of(), Set.of(), 1, 1, Map.of())),
                new GrantKeywordEffect(HASTE, Subjects.createdThisResolution(),
                        Duration.UNTIL_YOUR_NEXT_TURN)));
    }
}
