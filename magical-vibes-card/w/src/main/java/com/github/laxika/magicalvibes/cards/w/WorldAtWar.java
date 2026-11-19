package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "172")
@CardRegistration(set = "SLD", collectorNumber = "2208")
public class WorldAtWar extends Card {

    public WorldAtWar() {
        addSpellEffect(new AdditionalCombatMainPhaseEffect(
                1, new UntapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentAttackedThisTurnPredicate())))), false, false, true));
    }
}
