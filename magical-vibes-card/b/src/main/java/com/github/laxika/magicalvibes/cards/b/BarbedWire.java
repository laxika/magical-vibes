package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageBySelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "287")
public class BarbedWire extends Card {

    public BarbedWire() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new DealDamageToPlayersEffect(1, DamageRecipient.ACTIVE_PLAYER));
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new PreventNextDamageBySelfEffect()),
                "{2}: Prevent the next 1 damage that would be dealt by this artifact this turn."));
    }
}
