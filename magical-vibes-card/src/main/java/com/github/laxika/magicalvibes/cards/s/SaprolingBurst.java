package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithSourceCounterPTEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterOrSacrificeSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEM", collectorNumber = "113")
public class SaprolingBurst extends Card {

    public SaprolingBurst() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.FADE, new Fixed(7)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterOrSacrificeSelfEffect(CounterType.FADE));

        CreateTokenEffect saproling = new CreateTokenEffect(
                "Saproling", 0, 0, CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of());
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.FADE),
                        new CreateTokenWithSourceCounterPTEffect(CounterType.FADE, saproling)
                ),
                "Remove a fade counter from this enchantment: Create a green Saproling creature token."));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new DestroyTokensCreatedWithSourceEffect(true));
    }
}
