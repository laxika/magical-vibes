package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnGrantingEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapGrantingEquipmentCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "128")
public class FishingPole extends Card {

    public FishingPole() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{1}",
                        List.of(new TapGrantingEquipmentCost(),
                                new PutCountersOnGrantingEquipmentEffect(CounterType.BAIT)),
                        "{1}, {T}, Tap Fishing Pole: Put a bait counter on Fishing Pole."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        addEffect(EffectSlot.ON_ANY_PERMANENT_BECOMES_UNTAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsHostOfSourceAuraPredicate(),
                        ConditionalEffect.unless(
                                new SourceCounterThreshold(1, CounterType.BAIT),
                                SequenceEffect.of(
                                        new RemoveCounterFromSourceEffect(CounterType.BAIT, 1),
                                        new CreateTokenEffect("Fish", 1, 1, CardColor.BLUE,
                                                List.of(CardSubtype.FISH), Set.of(), Set.of())
                                )
                        )
                ));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
