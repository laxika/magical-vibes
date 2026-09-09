package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "9")
public class GruesomeSlaughter extends Card {

    public GruesomeSlaughter() {
        addEffect(EffectSlot.SPELL, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new DealDamageToTargetCreatureEffect(new SourcePower())),
                        "{T}: This creature deals damage equal to its power to target creature."
                ),
                GrantScope.OWN_CREATURES,
                new PermanentIsColorlessPredicate(),
                EffectDuration.UNTIL_END_OF_TURN
        ));
    }
}
