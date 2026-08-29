package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardAndUntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "PLS", collectorNumber = "139")
public class ForsakenCity extends Card {

    public ForsakenCity() {
        // This land doesn't untap during your untap step.
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        // At the beginning of your upkeep, you may exile a card from your hand. If you do, untap this land.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new ExileCardAndUntapSelfEffect(),
                "Exile a card from your hand to untap Forsaken City?"));

        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
