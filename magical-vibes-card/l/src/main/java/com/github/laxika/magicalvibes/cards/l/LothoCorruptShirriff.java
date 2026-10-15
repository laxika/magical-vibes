package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "34")
@CardRegistration(set = "HOC", collectorNumber = "74")
public class LothoCorruptShirriff extends Card {

    public LothoCorruptShirriff() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                2,
                List.of(new LoseLifeEffect(1), CreateTokenEffect.ofTreasureToken(1))
        ));
    }
}
