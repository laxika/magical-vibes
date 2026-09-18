package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEqualToLifeTotalAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.NefariousLichLifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsOrLoseGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "2ED", collectorNumber = "114")
@CardRegistration(set = "ME4", collectorNumber = "89")
public class Lich extends Card {

    public Lich() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEqualToLifeTotalAsEntersEffect());
        addEffect(EffectSlot.STATIC, new CantLoseGameFromLifeEffect());
        addEffect(EffectSlot.STATIC, new NefariousLichLifeGainReplacementEffect());
        addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE, new SacrificePermanentsOrLoseGameEffect(
                new EventValue(), new PermanentNotPredicate(new PermanentIsTokenPredicate())));
        addEffect(EffectSlot.ON_DEATH, new ControllerLosesGameEffect());
    }
}
