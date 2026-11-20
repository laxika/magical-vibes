package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "134")
@CardRegistration(set = "LCI", collectorNumber = "321")
public class BonehoardDracosaur extends Card {

    public BonehoardDracosaur() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new ExileTopCardMayPlayThisTurnEffect(2, false),
                new ConditionalEffect(
                        HappenedThisWay.toACard(StepKind.EXILE, new CardTypePredicate(CardType.LAND)),
                        new CreateTokenEffect("Dinosaur", 3, 1, CardColor.RED,
                                List.of(CardSubtype.DINOSAUR), Set.of(), Set.of())),
                new ConditionalEffect(
                        HappenedThisWay.toACard(StepKind.EXILE,
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                        CreateTokenEffect.ofTreasureToken(1))));
    }
}
