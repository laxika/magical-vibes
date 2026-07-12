package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "4")
public class BarrentonMedic extends Card {

    public BarrentonMedic() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new PreventNextDamageEffect(1)),
                "{T}: Prevent the next 1 damage that would be dealt to any target this turn."));
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PutCounterOnSourceCost(), new UntapPermanentsEffect(TapUntapScope.SELF)),
                "Put a -1/-1 counter on this creature: Untap this creature."));
    }
}
