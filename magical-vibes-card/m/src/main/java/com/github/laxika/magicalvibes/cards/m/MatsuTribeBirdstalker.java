package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "137")
public class MatsuTribeBirdstalker extends Card {

    public MatsuTribeBirdstalker() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new SkipNextUntapEffect(TapUntapScope.TARGET));

        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new GrantKeywordEffect(Keyword.REACH, GrantScope.SELF)),
                "{G}: This creature gains reach until end of turn."));
    }
}
