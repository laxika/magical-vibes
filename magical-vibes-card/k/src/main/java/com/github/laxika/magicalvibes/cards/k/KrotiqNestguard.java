package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "148")
public class KrotiqNestguard extends Card {

    public KrotiqNestguard() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new CanAttackAsThoughNoDefenderEffect()),
                "{2}{G}: This creature can attack this turn as though it didn't have defender."
        ));
    }
}
