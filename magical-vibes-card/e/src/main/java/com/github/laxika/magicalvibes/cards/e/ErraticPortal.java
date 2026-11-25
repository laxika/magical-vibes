package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "132")
@CardRegistration(set = "TPR", collectorNumber = "222")
@CardRegistration(set = "MB2", collectorNumber = "220")
public class ErraticPortal extends Card {

    public ErraticPortal() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.target()), "{1}", ReturnToHandEffect.target())
                        .withPrompt("Pay {1} to keep the target creature?")),
                "{1}, {T}: Return target creature to its owner's hand unless its controller pays {1}.",
                TargetFilters.creature()
        ));
    }
}
