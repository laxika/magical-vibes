package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "HOU", collectorNumber = "142")
public class ResoluteSurvivors extends Card {

    public ResoluteSurvivors() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true),
                "Exert Resolute Survivors as it attacks?"));

        addEffect(EffectSlot.ON_CONTROLLER_EXERTS, SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                new GainLifeEffect(1)));
    }
}
