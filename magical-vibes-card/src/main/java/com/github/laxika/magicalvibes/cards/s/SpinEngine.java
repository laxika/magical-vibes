package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "135")
public class SpinEngine extends Card {

    public SpinEngine() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new CantBlockSourceEffect(null)), "{R}: Target creature can't block Spin Engine this turn."));
    }
}
