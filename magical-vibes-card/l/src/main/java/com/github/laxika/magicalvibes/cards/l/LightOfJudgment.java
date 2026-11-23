package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "144")
public class LightOfJudgment extends Card {

    public LightOfJudgment() {
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(6))
                .addSpellEffect(ChoosePermanentsEffect.upTo(1,
                        Subjects.attachmentsOf(Subjects.target(), new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)),
                        "up to one Equipment attached to that creature"))
                .addSpellEffect(new DestroyTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE)));
    }
}
