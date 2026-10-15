package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "34")
@CardRegistration(set = "SOC", collectorNumber = "82")
public class PrismariPianist extends Card {

    public PrismariPianist() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        CreateTokenEffect oneElemental = new CreateTokenEffect(
                "Elemental", 1, 1, CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED),
                List.of(CardSubtype.ELEMENTAL));
        CreateTokenEffect threeElementals = new CreateTokenEffect(
                3, "Elemental", 1, 1, CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED),
                List.of(CardSubtype.ELEMENTAL));
        List<CardEffect> magecraft = List.of(
                new ConditionalEffect(new EventValueAtLeast(5),
                        new CreateTokenForTriggeringPlayerEffect(threeElementals)),
                new ConditionalEffect(new NotCondition(new EventValueAtLeast(5)),
                        new CreateTokenForTriggeringPlayerEffect(oneElemental)));

        // Whenever you cast an instant or sorcery spell, create a 1/1 blue and red Elemental
        // creature token. If that spell's mana value is 5 or greater, create three of those
        // tokens instead.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(instantOrSorcery, magecraft));
    }
}
