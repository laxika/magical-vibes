package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerControlsMoreLandsThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "90")
public class Rivalry extends Card {

    public Rivalry() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new ConditionalEffect(
                new ActivePlayerControlsMoreLandsThanEachOtherPlayer(),
                new DealDamageToPlayersEffect(2, DamageRecipient.ACTIVE_PLAYER)));
    }
}
