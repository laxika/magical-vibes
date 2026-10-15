package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "179")
public class ChocoboRacetrack extends Card {

    public ChocoboRacetrack() {
        CreateTokenEffect bird = new CreateTokenEffect(
                1, "Bird", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.BIRD), Set.of(), Set.of(),
                Map.of(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 0)));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, bird);
    }
}
