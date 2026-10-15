package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "58")
public class CrabUmbra extends Card {

    public CrabUmbra() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC, new TotemArmorEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new UntapPermanentsEffect(TapUntapScope.ENCHANTED)),
                "{2}{U}: Untap enchanted creature."
        ));
    }
}
