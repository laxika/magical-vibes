package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "18")
@CardRegistration(set = "INV", collectorNumber = "37")
public class Shackles extends Card {

    public Shackles() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(ReturnToHandEffect.self()),
                "{W}: Return this Aura to its owner's hand."
        ));
    }
}
