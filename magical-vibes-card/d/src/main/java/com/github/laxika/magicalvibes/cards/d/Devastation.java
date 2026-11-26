package com.github.laxika.magicalvibes.cards.d;

import java.util.List;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "123")
@CardRegistration(set = "S99", collectorNumber = "94")
@CardRegistration(set = "ME4", collectorNumber = "112")
public class Devastation extends Card {

    public Devastation() {
        addSpellEffect(DestroyTargetPermanentEffect.all(new PermanentAnyOfPredicate(
                List.of(new PermanentIsCreaturePredicate(), new PermanentIsLandPredicate()))));
    }
}
