package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "204")
public class FearlessSwashbuckler extends Card {

    public FearlessSwashbuckler() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HASTE,
                GrantScope.OWN_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)
        ));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new AllOf(List.of(
                        new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.PIRATE)),
                        new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))
                )), SequenceEffect.of(
                        new DrawCardEffect(3),
                        new DiscardEffect(2, DiscardRecipient.CONTROLLER)
                )));
    }
}
