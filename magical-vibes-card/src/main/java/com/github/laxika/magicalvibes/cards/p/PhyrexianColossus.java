package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "309")
public class PhyrexianColossus extends Card {

    public PhyrexianColossus() {
        // This creature doesn't untap during your untap step.
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        // Pay 8 life: Untap this creature.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(8),
                        new UntapPermanentsEffect(TapUntapScope.SELF)),
                "Pay 8 life: Untap this creature."));

        // This creature can't be blocked except by three or more creatures.
        addEffect(EffectSlot.STATIC, new CantBeBlockedByFewerThanNCreaturesEffect(3));
    }
}
