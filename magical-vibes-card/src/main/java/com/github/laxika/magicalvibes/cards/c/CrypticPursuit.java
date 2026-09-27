package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastFromHandTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "70")
@CardRegistration(set = "NCC", collectorNumber = "170")
public class CrypticPursuit extends Card {

    public CrypticPursuit() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastFromHandTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                List.of(new ManifestTopCardEffect())));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentIsFaceDownPredicate(),
                new ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect()));
    }
}
