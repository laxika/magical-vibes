package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "79")
@CardRegistration(set = "ME1", collectorNumber = "52")
public class Telekinesis extends Card {

    public Telekinesis() {
        target(TargetFilters.creature())
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(PreventDamageEffect.allCombatByTargetCreatures())
                .addSpellEffect(new SkipNextUntapEffect(TapUntapScope.TARGET, 2));
    }
}
