package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "117")
public class DeathbellowRaider extends Card {

    public DeathbellowRaider() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new RegenerateEffect()),
                "{2}{B}: Regenerate Deathbellow Raider."
        ));
    }
}
