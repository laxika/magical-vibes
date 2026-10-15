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
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "12")
@CardRegistration(set = "TDC", collectorNumber = "52")
public class AlignedHeart extends Card {

    public AlignedHeart() {
        Map<EffectSlot, CardEffect> monkTokenEffects = Map.of(
                EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new BoostSelfEffect(1, 1))));
        CreateTokenEffect monkTokens = new CreateTokenEffect(
                CardType.CREATURE, new CountersOnSource(CounterType.RALLY), "Monk", 1, 1,
                CardColor.WHITE, null, List.of(CardSubtype.MONK), Set.of(Keyword.PROWESS), Set.of(),
                false, false, monkTokenEffects, List.of(), false, false, false, 0, Set.of());

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new NthSpellCastTriggerEffect(2,
                        List.of(new PutCountersOnSelfEffect(CounterType.RALLY), monkTokens)));
    }
}
