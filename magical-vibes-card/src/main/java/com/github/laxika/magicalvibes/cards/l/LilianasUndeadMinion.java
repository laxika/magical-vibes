package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/** Back face of Liliana's Other Contract. */
public class LilianasUndeadMinion extends Card {

    public LilianasUndeadMinion() {
        addEffect(EffectSlot.STATIC, new CantLoseGameEffect());

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)),
                "+1: Each opponent loses 1 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new DestroyTargetPermanentEffect()),
                "−4: Destroy target creature.",
                TargetFilters.creature()
        ));
    }
}
