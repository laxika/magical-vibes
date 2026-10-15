package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "139")
public class PhyrexianIronfoot extends Card {

    public PhyrexianIronfoot() {
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{S}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{1}{S}: Untap this creature."
        ));
    }
}
