package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "194")
@CardRegistration(set = "GK1", collectorNumber = "87")
public class Brightflame extends Card {

    public Brightflame() {
        DealDamageEffect targetDamage = DealDamageEffect.toTargetCreature(new XValue());
        target(TargetFilters.creature()).bindEffect(targetDamage);
        addSimultaneousSpellEffects(targetDamage, DealDamageEffect.toEach(
                Subjects.otherCreaturesSharing(Subjects.target(), SharedQuality.COLOR),
                new XValue()));
        addSpellEffect(new GainLifeEffect(new AmountThisWay(StepKind.DEAL_DAMAGE)));
    }
}
