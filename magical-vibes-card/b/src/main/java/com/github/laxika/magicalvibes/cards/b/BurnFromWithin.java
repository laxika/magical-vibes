package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SOI", collectorNumber = "148")
@CardRegistration(set = "SIR", collectorNumber = "147")
public class BurnFromWithin extends Card {

    public BurnFromWithin() {
        addSpellEffect(DealDamageEffect.toAnyTarget(new XValue()).exilingDamagedCreaturesThatWouldDie());
        addSpellEffect(new RemoveKeywordEffect(Keyword.INDESTRUCTIBLE,
                Subjects.thisWay(StepKind.DEAL_DAMAGE, new PermanentIsCreaturePredicate())));
    }
}
