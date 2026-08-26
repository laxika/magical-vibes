package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "MKM", collectorNumber = "140")
public class PyrotechnicPerformer extends Card {

    public PyrotechnicPerformer() {
        addMorph("{R}");
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP,
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));
    }
}
