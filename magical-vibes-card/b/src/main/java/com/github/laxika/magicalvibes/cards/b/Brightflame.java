package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherCreaturesSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "194")
@CardRegistration(set = "GK1", collectorNumber = "87")
public class Brightflame extends Card {

    public Brightflame() {
        DealDamageToTargetCreatureEffect targetDamage = new DealDamageToTargetCreatureEffect(new XValue());
        target(TargetFilters.creature()).bindEffect(targetDamage);
        addSimultaneousSpellEffects(targetDamage, new DealDamageToPermanentsTargetControlsEffect(new XValue(),
                Subjects.otherCreaturesSharing(Subjects.target(), SharedQuality.COLOR)));
        addSpellEffect(new GainLifeEffect(new AmountThisWay(StepKind.DEAL_DAMAGE)));
    }
}
