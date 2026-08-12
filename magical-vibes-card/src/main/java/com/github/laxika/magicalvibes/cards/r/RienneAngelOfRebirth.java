package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

@CardRegistration(set = "M20", collectorNumber = "281")
public class RienneAngelOfRebirth extends Card {

    public RienneAngelOfRebirth() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES,
                new PermanentIsMulticoloredPredicate()));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardIsMulticoloredPredicate(),
                new RegisterDelayedReturnCardFromGraveyardToHandEffect(null)));
    }
}
