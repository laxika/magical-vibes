package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerUntilPlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "OHOP", collectorNumber = "3")
public class Agyrem extends Card {

    public Agyrem() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardColorPredicate(CardColor.WHITE),
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                        false, null, 0, null, null, false, true, false)));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardNotPredicate(new CardColorPredicate(CardColor.WHITE)),
                new RegisterDelayedReturnCardFromGraveyardToHandEffect(null)));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new GrantStaticEffectToPlayerUntilPlaneswalkEffect(
                new CreaturesCantAttackControllerUnlessPredicateEffect(
                        new PermanentNotPredicate(new PermanentTruePredicate()))));
    }
}
