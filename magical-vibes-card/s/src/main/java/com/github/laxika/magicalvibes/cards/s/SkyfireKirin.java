package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "113")
public class SkyfireKirin extends Card {

    public SkyfireKirin() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new MayEffect(
                        new GainControlOfTargetCreatureByCastSpellManaValueEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                                        new CardSubtypePredicate(CardSubtype.ARCANE)))),
                        "Gain control of target creature with that spell's mana value until end of turn?"));
    }
}
