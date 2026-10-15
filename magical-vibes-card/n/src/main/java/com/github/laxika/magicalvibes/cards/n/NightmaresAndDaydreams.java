package com.github.laxika.magicalvibes.cards.n;

import java.util.List;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.AnyGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

@CardRegistration(set = "TLE", collectorNumber = "94")
public class NightmaresAndDaydreams extends Card {

    private static final CardPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));
    private static final TargetFilter ANY_PLAYER = new PlayerPredicateTargetFilter(
            new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player");

    public NightmaresAndDaydreams() {
        addMillChapter(EffectSlot.SAGA_CHAPTER_I);
        addMillChapter(EffectSlot.SAGA_CHAPTER_II);
        addMillChapter(EffectSlot.SAGA_CHAPTER_III);

        AnyGraveyardAtLeast twentyCards = new AnyGraveyardAtLeast(20);
        addEffect(EffectSlot.SAGA_CHAPTER_IV, new ConditionalEffect(twentyCards, new DrawCardEffect(3)));
        addEffect(EffectSlot.SAGA_CHAPTER_IV,
                new ConditionalEffect(new NotCondition(twentyCards), new DrawCardEffect(1)));
    }

    private void addMillChapter(EffectSlot slot) {
        addEffect(slot, RegisterDelayedControllerSpellCastTriggerEffect.untilNextTurn(
                INSTANT_OR_SORCERY,
                List.of(new MillEffect(new EventValue(), MillRecipient.TARGET_PLAYER)),
                false,
                ANY_PLAYER));
    }
}
