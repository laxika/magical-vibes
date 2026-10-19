package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "CON", collectorNumber = "122")
public class RhoxBodyguard extends Card {

    public RhoxBodyguard() {
        // Exalted is auto-loaded from Scryfall as a keyword. Printed exalted fires per attacking ally and
        // boosts the attacker only when it attacks alone (see AvenSquire).
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
    }
}
