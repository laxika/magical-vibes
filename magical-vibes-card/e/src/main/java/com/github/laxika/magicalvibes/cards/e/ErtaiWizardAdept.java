package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "33")
public class ErtaiWizardAdept extends Card {

    public ErtaiWizardAdept() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}{U}",
                List.of(new CounterSpellEffect()),
                "{2}{U}{U}, {T}: Counter target spell."
        ));
    }
}
