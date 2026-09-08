package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastFromZonesEffect;

import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "241")
public class SoullessJailer extends Card {

    public SoullessJailer() {
        addEffect(EffectSlot.STATIC, new CardsCantEnterBattlefieldFromZonesEffect(
                null, Set.of(Zone.GRAVEYARD)));
        addEffect(EffectSlot.STATIC, new NoncreatureSpellsCantBeCastFromZonesEffect(
                Set.of(Zone.GRAVEYARD, Zone.EXILE)));
    }
}
