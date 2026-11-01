package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "216")
@CardRegistration(set = "KHM", collectorNumber = "324")
@CardRegistration(set = "SLD", collectorNumber = "1807")
@CardRegistration(set = "DSC", collectorNumber = "223")
@CardRegistration(set = "SCD", collectorNumber = "5")
public class KardurDoomscourge extends Card {

    public KardurDoomscourge() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                GoadCreaturesUntilNextTurnEffect.requirementOnly(new PermanentNotPredicate(
                        new PermanentControlledBySourceControllerPredicate())));
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), new TriggeringPermanentConditionalEffect(
                new PermanentIsAttackingPredicate(),
                SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1))));
    }
}
