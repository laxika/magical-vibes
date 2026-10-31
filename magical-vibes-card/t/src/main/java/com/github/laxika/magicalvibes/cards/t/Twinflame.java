package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "115")
@CardRegistration(set = "SLD", collectorNumber = "1810")
@CardRegistration(set = "2X2", collectorNumber = "129")
@CardRegistration(set = "SOC", collectorNumber = "258")
public class Twinflame extends Card {

    public Twinflame() {
        setAdditionalManaCostPerExtraTarget("{2}{R}");
        target(TargetFilters.creatureYouControl(), 0, 99)
                .addSpellEffect(new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(), Set.of(), null, null, Map.of(), false, true, false, false, false, false,
                        null, Set.of(Keyword.HASTE)));
    }
}
