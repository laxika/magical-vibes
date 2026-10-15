package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentNameEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "152")
public class AwakeningOfVituGhazi extends Card {

    public AwakeningOfVituGhazi() {
        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 9))
                .addEffect(EffectSlot.SPELL, new AnimatePermanentsEffect(
                        0, 0,
                        List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE),
                        null, Set.of(), GrantScope.TARGET, EffectDuration.PERMANENT))
                .addEffect(EffectSlot.SPELL,
                        new SetTargetPermanentSupertypeEffect(CardSupertype.LEGENDARY, true))
                .addEffect(EffectSlot.SPELL, new SetTargetPermanentNameEffect("Vitu-Ghazi"));
    }
}
