package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "FRF", collectorNumber = "158")
public class WarFlare extends Card {

    public WarFlare() {
        addSpellEffect(BoostEffect.creaturesYouControl(2, 1));
        addSpellEffect(new UntapPermanentsEffect(
                Subjects.controlledBy(Subjects.you(), new PermanentIsCreaturePredicate())));
    }
}
