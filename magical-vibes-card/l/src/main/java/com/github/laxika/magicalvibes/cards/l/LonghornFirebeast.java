package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "103")
public class LonghornFirebeast extends Card {

    public LonghornFirebeast() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_OPPONENT,
                new DealDamageToPlayersEffect(5, Subjects.acceptedPlayers()),
                "Have it deal 5 damage to you? If a player does, it is sacrificed.",
                new SacrificeSelfEffect(), null));
    }
}
