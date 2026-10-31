package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "GRN", collectorNumber = "95")
public class CosmotronicWave extends Card {

    public CosmotronicWave() {
        PermanentNotPredicate opponentPermanent =
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate());
        addSpellEffect(new MassDamageEffect(1, false, false, opponentPermanent));
        addSpellEffect(new CantBlockThisTurnEffect(TapUntapScope.ALL_CREATURES, opponentPermanent));
    }
}
