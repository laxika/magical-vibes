package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterAsCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "100")
@CardRegistration(set = "WHO", collectorNumber = "705")
public class DisplacedDinosaurs extends Card {

    public DisplacedDinosaurs() {
        addEffect(EffectSlot.STATIC, new ControlledPermanentsEnterAsCreatureEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsHistoricPredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                7,
                7,
                List.of(CardSubtype.DINOSAUR)
        ));
    }
}
