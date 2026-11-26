package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NEO", collectorNumber = "176")
public class BoonOfBoseiju extends Card {

    public BoonOfBoseiju() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(
                        new GreatestManaValueAmongControlled(new PermanentTruePredicate()),
                        new GreatestManaValueAmongControlled(new PermanentTruePredicate())))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()));
    }
}
