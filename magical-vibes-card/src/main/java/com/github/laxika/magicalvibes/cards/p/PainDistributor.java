package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "33")
@CardRegistration(set = "MOC", collectorNumber = "120")
public class PainDistributor extends Card {

    public PainDistributor() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new NthSpellCastTriggerEffect(1, List.of(CreateTokenEffect.ofTreasureToken(1))));
        addEffect(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
