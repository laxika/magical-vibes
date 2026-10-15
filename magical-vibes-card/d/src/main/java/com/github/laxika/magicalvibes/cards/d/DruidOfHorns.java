package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "176")
public class DruidOfHorns extends Card {

    public DruidOfHorns() {
        // Whenever you cast an Aura spell that targets this creature, create a 3/3 green Beast creature token.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardSubtypePredicate(CardSubtype.AURA),
                List.of(new CreateTokenEffect("Beast", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.BEAST), Set.of(), Set.of())),
                new StackEntryTargetsSourcePredicate()
        ));
    }
}
