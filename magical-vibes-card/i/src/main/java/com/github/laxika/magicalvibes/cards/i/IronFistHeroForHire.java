package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "692")
public class IronFistHeroForHire extends Card {

    public IronFistHeroForHire() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new BoostSelfEffect(1, 1))
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}{R}",
                List.of(
                        new DealDividedDamageEffect(
                                new Fixed(5), null, DivisionMode.CHOSEN, null, 5, true, false, false),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 5)
                ),
                "Power-up — {7}{R}: Iron Fist deals 5 damage divided as you choose among up to five targets. "
                        + "Put five +1/+1 counters on Iron Fist. Activate each power-up ability only once. "
                        + "Reduce the cost by his mana cost if he entered this turn.",
                null, null, null, null, List.of(), 0, 5
        ).withPowerUp());
    }
}
