package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "39")
public class WarScreecher extends Card {

    public WarScreecher() {
        // {5}{W}, {T}: Other creatures you control get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1,
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                "{5}{W}, {T}: Other creatures you control get +1/+1 until end of turn."
        ));
    }
}
