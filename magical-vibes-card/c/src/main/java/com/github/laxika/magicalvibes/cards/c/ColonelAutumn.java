package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "PIP", collectorNumber = "98")
@CardRegistration(set = "PIP", collectorNumber = "411")
@CardRegistration(set = "PIP", collectorNumber = "626")
@CardRegistration(set = "PIP", collectorNumber = "939")
public class ColonelAutumn extends Card {

    public ColonelAutumn() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"));

        addEffect(EffectSlot.STATIC,
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(new ExploitEffect(), "Sacrifice a creature?"),
                        GrantScope.OWN_CREATURES,
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));

        addEffect(EffectSlot.ON_ALLY_CREATURE_EXPLOITS,
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
    }
}
