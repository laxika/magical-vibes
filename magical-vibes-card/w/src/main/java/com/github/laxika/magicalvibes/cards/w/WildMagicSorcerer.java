package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2494")
@CardRegistration(set = "AFC", collectorNumber = "36")
public class WildMagicSorcerer extends Card {

    public WildMagicSorcerer() {
        addTrigger(Triggers.youCastSpell().castFrom(Zone.EXILE).nthThisTurn(1).grantedToTheSpell(),
                new CascadeEffect());
    }
}
