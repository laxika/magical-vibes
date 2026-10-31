package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "37")
public class ExpelFromOrazca extends Card {

    public ExpelFromOrazca() {
        addSpellEffect(new AscendEffect());
        target(TargetFilters.nonlandPermanent())
                .addSpellEffect(new ConditionalEffect(new ControllerHasCityBlessing(),
                        new MayEffect(new PutTargetOnTopOfLibraryEffect(),
                                "Put that permanent on top of its owner's library?")))
                .addSpellEffect(ReturnToHandEffect.target());
    }
}
