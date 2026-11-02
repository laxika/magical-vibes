package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardAndUntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLS", collectorNumber = "139")
public class ForsakenCity extends Card {

    public ForsakenCity() {
        // This land doesn't untap during your untap step.
        addStaticEffect(DoesntUntapEffect.self());

        // At the beginning of your upkeep, you may exile a card from your hand. If you do, untap this land.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new ExileCardAndUntapSelfEffect(),
                "Exile a card from your hand to untap Forsaken City?"));

        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
