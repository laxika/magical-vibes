package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "207")
public class GoblinSharpshooter extends Card {

    public GoblinSharpshooter() {
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new UntapPermanentsEffect(TapUntapScope.SELF));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: This creature deals 1 damage to any target."
        ));
    }
}
