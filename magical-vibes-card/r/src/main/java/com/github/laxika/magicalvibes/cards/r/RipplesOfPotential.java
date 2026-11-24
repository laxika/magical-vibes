package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

@CardRegistration(set = "SLD", collectorNumber = "2453")
@CardRegistration(set = "LCC", collectorNumber = "45")
@CardRegistration(set = "LCC", collectorNumber = "77")
@CardRegistration(set = "EOC", collectorNumber = "77")
public class RipplesOfPotential extends Card {

    public RipplesOfPotential() {
        // "Proliferate, then choose any number of permanents you control that had a counter put on them this way.
        // Those permanents phase out."
        addSpellEffect(new ProliferateEffect());
        addSpellEffect(ChoosePermanentsEffect.anyNumber(
                Subjects.thisWay(StepKind.PUT_COUNTERS, new PermanentControlledBySourceControllerPredicate()),
                "any number of permanents you control that had a counter put on them this way"));
        addSpellEffect(new PhaseOutEffect(Subjects.thisWay(StepKind.CHOOSE)));
    }
}
