package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawFromBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WHO", collectorNumber = "152")
public class RiverSong extends Card {

    public RiverSong() {
        addEffect(EffectSlot.STATIC, new DrawFromBottomOfLibraryEffect());

        CardEffect trigger = SequenceEffect.of(
                new PutCountersOnSourceEffect(1, 1, 1),
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_OPPONENT_SCRIES, trigger);
        addEffect(EffectSlot.ON_OPPONENT_SURVEILS, trigger);
        addEffect(EffectSlot.ON_OPPONENT_SEARCHES_LIBRARY, trigger);
    }
}
