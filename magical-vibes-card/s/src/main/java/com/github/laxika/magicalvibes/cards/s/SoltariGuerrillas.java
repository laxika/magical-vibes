package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromChosenSourceToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "272")
@CardRegistration(set = "TPR", collectorNumber = "212")
public class SoltariGuerrillas extends Card {

    public SoltariGuerrillas() {
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(RedirectNextDamageFromChosenSourceToSelfEffect.to(Subjects.source(), Subjects.target())
                        .dealtTo(Subjects.eachOpponent())
                        .combatDamageOnly()),
                "{0}: The next time this creature would deal combat damage to an opponent this turn, "
                        + "it deals that damage to target creature instead."));
    }
}
