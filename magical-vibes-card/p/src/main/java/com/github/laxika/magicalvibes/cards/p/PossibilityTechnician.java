package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayWhileControllingSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "153")
public class PossibilityTechnician extends Card {

    public PossibilityTechnician() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{R}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTopCardMayPlayWhileControllingSubtypeEffect(CardSubtype.KAVU));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.KAVU),
                        new ExileTopCardMayPlayWhileControllingSubtypeEffect(CardSubtype.KAVU)));
    }
}
