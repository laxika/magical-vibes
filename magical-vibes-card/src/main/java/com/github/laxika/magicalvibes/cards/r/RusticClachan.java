package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "150")
public class RusticClachan extends Card {

    public RusticClachan() {
        // As this land enters, you may reveal a Kithkin card from your hand.
        // If you don't, this land enters tapped.
        addEffect(EffectSlot.STATIC, new RevealSubtypeOrEntersTappedEffect(CardSubtype.KITHKIN));

        // {T}: Add {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        // Reinforce 1—{1}{W} ({1}{W}, Discard this card: Put a +1/+1 counter on target creature.)
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                "Reinforce 1—{1}{W} ({1}{W}, Discard this card: Put a +1/+1 counter on target creature.)",
                TargetFilters.creature()));
    }
}
