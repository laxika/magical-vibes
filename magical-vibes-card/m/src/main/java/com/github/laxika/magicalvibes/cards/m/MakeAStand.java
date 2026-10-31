package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "M19", collectorNumber = "26")
@CardRegistration(set = "OGW", collectorNumber = "26")
public class MakeAStand extends Card {

    public MakeAStand() {
        // Creatures you control get +1/+0 and gain indestructible until end of turn.
        addSpellEffect(new BoostAllOwnCreaturesEffect(1, 0));
        addSpellEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES));
    }
}
