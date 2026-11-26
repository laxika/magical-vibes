package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "164")
@CardRegistration(set = "KLR", collectorNumber = "173")
public class OrnamentalCourage extends Card {

    public OrnamentalCourage() {
        target(TargetFilters.creature()).addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(BoostEffect.target(1, 3));
    }
}
