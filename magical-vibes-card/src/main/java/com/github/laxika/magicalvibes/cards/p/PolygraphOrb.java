package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TormentOfHailfireEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "99")
public class PolygraphOrb extends Card {

    public PolygraphOrb() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(4, 2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(2));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new CollectEvidenceCost(3),
                        TormentOfHailfireEffect.once(3, new PermanentIsCreaturePredicate())),
                "{2}, {T}, Collect evidence 3: Each opponent loses 3 life unless they discard a card or "
                        + "sacrifice a creature."
        ));
    }
}
