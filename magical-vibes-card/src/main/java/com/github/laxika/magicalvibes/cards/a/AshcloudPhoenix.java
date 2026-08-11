package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldFaceDownEffect;

@CardRegistration(set = "KTK", collectorNumber = "99")
public class AshcloudPhoenix extends Card {

    public AshcloudPhoenix() {
        addMorph("{4}{R}{R}");
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceCardFromGraveyardToBattlefieldFaceDownEffect());
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new DealDamageToPlayersEffect(2, DamageRecipient.EACH_PLAYER));
    }
}
