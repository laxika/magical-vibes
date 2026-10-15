package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "103")
public class OnyxMage extends Card {

    public OnyxMage() {
        // {1}{B}: Target creature you control gains deathtouch until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET)),
                "{1}{B}: Target creature you control gains deathtouch until end of turn.",
                TargetFilters.creatureYouControl()));
    }
}
