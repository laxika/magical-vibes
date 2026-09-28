package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "750")
public class IronSuitcase extends Card {

    public IronSuitcase() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new AnimatePermanentsEffect(
                        3, 3, List.of(CardSubtype.CONSTRUCT), Set.of(Keyword.FLYING), null)),
                "{3}: This artifact becomes a 3/3 Construct artifact creature with flying until end of turn."
        ));
    }
}
