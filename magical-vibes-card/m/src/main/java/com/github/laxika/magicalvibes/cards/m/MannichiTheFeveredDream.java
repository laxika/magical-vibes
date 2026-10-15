package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SwitchAllCreaturesPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "112")
public class MannichiTheFeveredDream extends Card {

    public MannichiTheFeveredDream() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}",
                List.of(new SwitchAllCreaturesPowerToughnessEffect()),
                "{1}{R}: Switch each creature's power and toughness until end of turn."));
    }
}
