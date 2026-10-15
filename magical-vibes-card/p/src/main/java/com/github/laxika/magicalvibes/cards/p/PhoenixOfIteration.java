package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "24")
public class PhoenixOfIteration extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public PhoenixOfIteration() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, opusTrigger());
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL, opusTrigger());
    }

    private static SpellCastTriggerEffect opusTrigger() {
        return new SpellCastTriggerEffect(INSTANT_OR_SORCERY, List.of(
                new PerpetuallyBoostSourceEffect(1, 1),
                new ConditionalEffect(
                        new SpellManaSpentAtLeast(5),
                        new MayEffect(
                                SequenceEffect.of(
                                        new ExileSelfEffect(),
                                        new ExileSourceCardFromGraveyardEffect(),
                                        new ReturnSourceCardFromExileToBattlefieldEffect(true)),
                                "Exile Phoenix of Iteration, then return it to the battlefield tapped?"))
        ));
    }
}
