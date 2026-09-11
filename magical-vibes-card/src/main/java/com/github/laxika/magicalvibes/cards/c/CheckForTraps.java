package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardMatches;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "92")
public class CheckForTraps extends Card {

    public CheckForTraps() {
        ImprintedCardMatches instantOrFlash = new ImprintedCardMatches(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardKeywordPredicate(Keyword.FLASH))),
                "an instant card or a card with flash");
        SpellTarget opponentTarget = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"));
        opponentTarget.addEffect(EffectSlot.SPELL,
                new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), HandChoiceDestination.EXILE, true));
        opponentTarget.addEffect(EffectSlot.SPELL,
                new ConditionalEffect(instantOrFlash,
                        new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)));
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new NotCondition(instantOrFlash), new LoseLifeEffect(1)));
    }
}
