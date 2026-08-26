package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpellCastDamageToCasterEffect;

@CardRegistration(set = "LCI", collectorNumber = "165")
@CardRegistration(set = "LCI", collectorNumber = "323")
public class ScytheclawRaptor extends Card {

    public ScytheclawRaptor() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                SpellCastDamageToCasterEffect.whenCasterIsNotActiveTurn(4));
    }
}
