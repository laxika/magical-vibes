package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "123")
public class YaroksFenlurker extends Card {

    public YaroksFenlurker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EachOpponentExilesFromHandEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new BoostSelfEffect(1, 1)),
                "{2}{B}: This creature gets +1/+1 until end of turn."
        ));
    }
}
