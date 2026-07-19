package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDamageAtNextUpkeepUnlessPaysEffect;

@CardRegistration(set = "CON", collectorNumber = "70")
public class QuenchableFire extends Card {

    public QuenchableFire() {
        // Deals 3 damage to target player or planeswalker, then registers a delayed trigger: at the
        // beginning of your next upkeep it deals an additional 3 damage to that same player or
        // planeswalker unless that player (or that planeswalker's controller) pays {U} first. The
        // registrar piggybacks on the damage effect's target (declares no target of its own).
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerOrPlaneswalkerEffect(3));
        addEffect(EffectSlot.SPELL, new RegisterDamageAtNextUpkeepUnlessPaysEffect(3, "{U}"));
    }
}
