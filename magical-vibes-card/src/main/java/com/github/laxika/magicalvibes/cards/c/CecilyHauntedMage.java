package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "343")
public class CecilyHauntedMage extends Card {

    public CecilyHauntedMage() {
        addEffect(EffectSlot.STATIC, new SetControllerMaximumHandSizeEffect(11));
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new DrawCardEffect(1),
                new LoseLifeEffect(1, LoseLifeRecipient.CONTROLLER),
                ConditionalEffect.unless(
                        new CardsInHandAtLeast(11),
                        new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY)))))));
    }
}
