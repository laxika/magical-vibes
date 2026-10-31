package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FDN", collectorNumber = "53")
public class UnchartedVoyage extends Card {

    public UnchartedVoyage() {
        target(TargetFilters.creature())
                .addSpellEffect(new PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect())
                .addSpellEffect(new SurveilEffect(1));
    }
}
