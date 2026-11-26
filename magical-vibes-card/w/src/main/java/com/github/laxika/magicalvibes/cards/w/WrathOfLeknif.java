package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "MB2", collectorNumber = "366")
@CardRegistration(set = "MB2", collectorNumber = "605")
public class WrathOfLeknif extends Card {

    public WrathOfLeknif() {
        // Destroy all creatures. They can't be regenerated.
        addSpellEffect(DestroyTargetPermanentEffect.all(
                new PermanentIsCreaturePredicate(), true));
        // Untap up to four lands you control.
        addSpellEffect(new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(),
                new PermanentIsLandPredicate()), null, 4));
    }
}
