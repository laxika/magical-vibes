package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;

@CardRegistration(set = "SLD", collectorNumber = "860")
@CardRegistration(set = "SLD", collectorNumber = "1301")
@CardRegistration(set = "SLD", collectorNumber = "2184")
@CardRegistration(set = "C13", collectorNumber = "201")
public class NekusarTheMindrazer extends Card {

    public NekusarTheMindrazer() {
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new DrawCardForTargetPlayerEffect(1));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER));
    }
}
