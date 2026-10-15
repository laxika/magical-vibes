package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "28")
public class CephalidIllusionist extends Card {

    public CephalidIllusionist() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                new MillEffect(3, MillRecipient.CONTROLLER));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(
                        PreventDamageEffect.allCombatToTargetCreatures(),
                        PreventDamageEffect.allCombatByTargetCreatures()),
                "{2}{U}, {T}: Prevent all combat damage that would be dealt to and dealt by target creature you control this turn.",
                TargetFilters.creatureYouControl()
        ));
    }
}
