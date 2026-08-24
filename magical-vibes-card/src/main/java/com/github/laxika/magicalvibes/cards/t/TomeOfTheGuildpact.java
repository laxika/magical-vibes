package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "242")
public class TomeOfTheGuildpact extends Card {

    public TomeOfTheGuildpact() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsMulticoloredPredicate(),
                List.of(new DrawCardEffect())
        ));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
