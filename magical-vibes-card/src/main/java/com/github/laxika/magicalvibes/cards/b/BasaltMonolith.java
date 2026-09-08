package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "SUM", collectorNumber = "235")
public class BasaltMonolith extends Card {

    public BasaltMonolith() {
        // This artifact doesn't untap during your untap step.
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        // {T}: Add {C}{C}{C}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS, 3));

        // {3}: Untap this artifact.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{3}: Untap Basalt Monolith."
        ));
    }
}
