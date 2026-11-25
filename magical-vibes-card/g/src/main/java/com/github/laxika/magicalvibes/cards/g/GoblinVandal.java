package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "105")
@CardRegistration(set = "ATH", collectorNumber = "41")
public class GoblinVandal extends Card {

    public GoblinVandal() {
        // Whenever this creature attacks and isn't blocked, you may pay {R}. If you do, destroy
        // target artifact defending player controls and this creature assigns no combat damage
        // this turn. "If you do" is the payment of {R} (CR 118.12), not the destruction.
        addTrigger(Triggers.attacksUnblocked(),
                UnlessPaysEffect.youMayPay("{R}", SequenceEffect.of(
                                new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentControlledByDefendingPlayerPredicate()))),
                                new AssignNoCombatDamageEffect())).withPrompt("pay {R} to destroy target artifact defending player controls"));
    }
}
