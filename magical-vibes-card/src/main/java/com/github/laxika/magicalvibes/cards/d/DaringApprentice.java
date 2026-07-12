package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "72")
@CardRegistration(set = "8ED", collectorNumber = "73")
public class DaringApprentice extends Card {

    public DaringApprentice() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new CounterSpellEffect()),
                "{T}, Sacrifice Daring Apprentice: Counter target spell."
        ));
    }
}
