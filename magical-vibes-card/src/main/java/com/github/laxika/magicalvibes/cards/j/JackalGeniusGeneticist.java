package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsSourcePowerPredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "131")
public class JackalGeniusGeneticist extends Card {

    public JackalGeniusGeneticist() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(CopySpellEffect.asTokenWithoutLegendary(), new PutCountersOnSourceEffect(1, 1, 1)),
                new StackEntryManaValueEqualsSourcePowerPredicate()));
    }
}
