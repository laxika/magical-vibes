package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BubbleUp;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "40")
public class AquaticAlchemistBubbleUp extends Card {

    public AquaticAlchemistBubbleUp() {
        setBackFaceCard(new BubbleUp());
        addCastingOption(new AdventureCast("{2}{U}"));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new OncePerTurnTriggerEffect(
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        List.of(new BoostSelfEffect(2, 0))
                )));
    }

    @Override
    public String getBackFaceClassName() {
        return "BubbleUp";
    }
}
