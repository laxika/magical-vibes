package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopySpellForAnotherOpponentPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "72")
@CardRegistration(set = "M3C", collectorNumber = "124")
public class ExterminatorMagmarch extends Card {

    public ExterminatorMagmarch() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CopySpellForAnotherOpponentPermanentEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Exterminator Magmarch."
        ));
    }
}
