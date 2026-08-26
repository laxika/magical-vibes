package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RememberTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "67")
public class LaquatussChampion extends Card {

    public LaquatussChampion() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RememberTargetPlayerEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(6, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new TargetPlayerGainsLifeEffect(6));
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()),
                "{B}: Regenerate Laquatus's Champion."));
    }
}
