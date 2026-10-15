package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "220")
public class CrazedArmodon extends Card {

    public CrazedArmodon() {
        // {G}: This creature gets +3/+0 and gains trample until end of turn. Destroy this creature
        // at the beginning of the next end step. Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{G}",
                List.of(
                        new BoostSelfEffect(3, 0),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF),
                        new DestroySelfAtEndStepEffect()
                ),
                "{G}: This creature gets +3/+0 and gains trample until end of turn. Destroy this "
                        + "creature at the beginning of the next end step. Activate only once each turn.",
                1));
    }
}
