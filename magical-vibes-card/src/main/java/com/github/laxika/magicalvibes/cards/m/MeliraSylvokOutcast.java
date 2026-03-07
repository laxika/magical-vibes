package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantHaveMinusOneMinusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PlayerCantGetPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

@CardRegistration(set = "NPH", collectorNumber = "115")
public class MeliraSylvokOutcast extends Card {

    public MeliraSylvokOutcast() {
        // You can't get poison counters.
        addEffect(EffectSlot.STATIC, new PlayerCantGetPoisonCountersEffect());

        // Creatures you control can't have -1/-1 counters put on them.
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CantHaveMinusOneMinusOneCountersEffect(), GrantScope.OWN_CREATURES));

        // Creatures your opponents control lose infect.
        addEffect(EffectSlot.STATIC, new RemoveKeywordEffect(Keyword.INFECT, GrantScope.OPPONENT_CREATURES));
    }
}
