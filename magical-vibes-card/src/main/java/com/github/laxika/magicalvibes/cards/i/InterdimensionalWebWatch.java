package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "165")
@CardRegistration(set = "SPM", collectorNumber = "278")
public class InterdimensionalWebWatch extends Card {

    public InterdimensionalWebWatch() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTopCardsMayPlayUntilNextTurnEffect(2));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2, ManaSpendRestriction.EXILED_SPELL_ONLY)),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast spells from exile."
        ));
    }
}
