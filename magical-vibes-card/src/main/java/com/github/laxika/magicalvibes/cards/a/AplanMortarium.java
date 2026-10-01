package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "568")
public class AplanMortarium extends Card {

    public AplanMortarium() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.EXPOSURE),
                new LoseLifeEffect(new CountersOnSource(CounterType.EXPOSURE), LoseLifeRecipient.CONTROLLER)));

        CreateTokenEffect alienAngel = new CreateTokenEffect(
                2,
                "Alien Angel",
                2,
                2,
                CardColor.BLACK,
                List.of(CardSubtype.ALIEN, CardSubtype.ANGEL),
                Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE),
                Set.of(CardType.ARTIFACT),
                Map.of(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                        new SpellCastTriggerEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                List.of(new SetCardTypesUntilEndOfTurnEffect(
                                        Set.of(CardType.ARTIFACT), GrantScope.SELF)))));
        addEffect(EffectSlot.CHAOS_TRIGGERED, alienAngel);
    }
}
