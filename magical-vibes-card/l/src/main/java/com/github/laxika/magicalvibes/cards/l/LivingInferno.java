package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "GPT", collectorNumber = "69")
public class LivingInferno extends Card {

    public LivingInferno() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        DealDividedDamageEffect.chosenAmongTargetCreatures(new SourcePower()),
                        DealDamageEffect.to(Subjects.source(), new SourcePower()).dealtBy(Subjects.target())),
                "{T}: This creature deals damage equal to its power divided as you choose among any number of target creatures. Each of those creatures deals damage equal to its power to this creature.",
                TargetFilters.creature(), null, null, null, List.of(), 0, 99
        ));
    }
}
