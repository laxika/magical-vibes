package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastFromHandTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "70")
@CardRegistration(set = "NCC", collectorNumber = "170")
public class CrypticPursuit extends Card {

    public CrypticPursuit() {
        addTrigger(Triggers.youCastSpell(), new SpellCastFromHandTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                List.of(new ManifestTopCardEffect())));
        addTrigger(Triggers.creatureDies(new PermanentIsFaceDownPredicate(), ObjectRelation.YOU_CONTROL),
                new ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect());
    }
}
