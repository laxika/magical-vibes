package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventRegenerationOfCreaturesDamagedBySourceThisTurnEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "176")
public class BoneShaman extends Card {

    public BoneShaman() {
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new PreventRegenerationOfCreaturesDamagedBySourceThisTurnEffect()),
                "{B}: Until end of turn, this creature gains \"Creatures dealt damage by this creature this turn can't be regenerated this turn.\""));
    }
}
