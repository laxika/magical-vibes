package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "131")
public class AkkiRonin extends Card {

    public AkkiRonin() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect(
                        new com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate(List.of(
                                new com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate(CardSubtype.SAMURAI),
                                new com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate(CardSubtype.WARRIOR))),
                        new ConditionalEffect(new AttacksAlone(), new MayEffect(
                                new DiscardAndDrawCardEffect(),
                                "Discard a card to draw a card?"))));
    }
}
