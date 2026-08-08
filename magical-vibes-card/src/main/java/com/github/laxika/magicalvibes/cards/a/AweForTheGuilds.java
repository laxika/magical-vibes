package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMonocoloredPredicate;

@CardRegistration(set = "DGM", collectorNumber = "31")
public class AweForTheGuilds extends Card {

    public AweForTheGuilds() {
        // Monocolored creatures can't block this turn.
        addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(TapUntapScope.ALL_CREATURES,
                new PermanentIsMonocoloredPredicate()));
    }
}
