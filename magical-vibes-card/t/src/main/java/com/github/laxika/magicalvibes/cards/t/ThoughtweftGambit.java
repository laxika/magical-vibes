package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "154")
public class ThoughtweftGambit extends Card {

    public ThoughtweftGambit() {
        // Tap all creatures your opponents control (every creature not controlled by you).
        addSpellEffect(new TapPermanentsEffect(
                Subjects.controlledBy(Subjects.eachPlayer(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))))));
        // Untap all creatures you control.
        addSpellEffect(new UntapPermanentsEffect(
                Subjects.controlledBy(Subjects.you(), new PermanentIsCreaturePredicate())));
    }
}
