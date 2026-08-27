package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "11")
public class LionheartMaverick extends Card {

    public LionheartMaverick() {
        addActivatedAbility(new ActivatedAbility(false, "{4}{W}", List.of(new BoostSelfEffect(1, 2)),
                "{4}{W}: This creature gets +1/+2 until end of turn."));
    }
}
