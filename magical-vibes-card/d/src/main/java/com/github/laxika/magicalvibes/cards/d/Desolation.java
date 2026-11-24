package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerTappedLandForManaThisTurnPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "58")
public class Desolation extends Card {

    public Desolation() {
        // At the beginning of each end step, each player who tapped a land for mana this turn
        // sacrifices a land of their choice. This enchantment deals 2 damage to each player who
        // sacrificed a Plains this way.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(),
                        Subjects.matching(Subjects.eachPlayer(), new PlayerTappedLandForManaThisTurnPredicate())),
                new DealDamageToPlayersEffect(2, Subjects.playersThisWay(StepKind.SACRIFICE,
                        new PermanentHasSubtypePredicate(CardSubtype.PLAINS)))));
    }
}
