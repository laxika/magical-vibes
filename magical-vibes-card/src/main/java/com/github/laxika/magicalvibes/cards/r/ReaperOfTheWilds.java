package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "201")
public class ReaperOfTheWilds extends Card {

    public ReaperOfTheWilds() {
        // Whenever another creature dies, scry 1.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new ScryEffect(1));

        // {B}: This creature gains deathtouch until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)),
                "{B}: This creature gains deathtouch until end of turn."));

        // {1}{G}: This creature gains hexproof until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)),
                "{1}{G}: This creature gains hexproof until end of turn."));
    }
}
