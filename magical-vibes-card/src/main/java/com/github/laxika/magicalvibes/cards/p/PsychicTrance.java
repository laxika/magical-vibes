package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "102")
public class PsychicTrance extends Card {

    public PsychicTrance() {
        addEffect(EffectSlot.SPELL, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new CounterSpellEffect()),
                        "{T}: Counter target spell."
                ),
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.WIZARD),
                EffectDuration.UNTIL_END_OF_TURN
        ));
    }
}
