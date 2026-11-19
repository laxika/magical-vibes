package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MRD", collectorNumber = "268")
public class TowerOfMurmurs extends Card {

    public TowerOfMurmurs() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{8}",
                List.of(new MillEffect(8, Subjects.targetPlayer())),
                "{8}, {T}: Target player mills eight cards."
        ));
    }
}
