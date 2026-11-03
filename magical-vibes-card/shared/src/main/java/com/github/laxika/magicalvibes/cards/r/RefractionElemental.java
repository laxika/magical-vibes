package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


public class RefractionElemental extends Card {

    public RefractionElemental() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysLifeEffect(new Fixed(2)));
        addTrigger(Triggers.youCastSpell().anySpell(), new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
    }
}
