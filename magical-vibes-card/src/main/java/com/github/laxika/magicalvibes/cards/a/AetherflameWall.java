package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockCreaturesWithShadowEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "142")
public class AetherflameWall extends Card {

    public AetherflameWall() {
        addEffect(EffectSlot.STATIC, new CanBlockCreaturesWithShadowEffect());
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)), "{R}: This creature gets +1/+0 until end of turn."));
    }
}
