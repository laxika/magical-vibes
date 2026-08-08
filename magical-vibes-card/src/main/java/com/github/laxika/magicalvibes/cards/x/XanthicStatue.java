package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WTH", collectorNumber = "163")
public class XanthicStatue extends Card {

    public XanthicStatue() {
        // {5}: Until end of turn, this artifact becomes an 8/8 Golem artifact creature with trample.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new AnimatePermanentsEffect(
                        8, 8, List.of(CardSubtype.GOLEM), Set.of(Keyword.TRAMPLE), null,
                        Set.of(CardType.ARTIFACT))),
                "{5}: Until end of turn, this artifact becomes an 8/8 Golem artifact creature with trample."
        ));
    }
}
