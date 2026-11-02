package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "137")
public class MatsuTribeBirdstalker extends Card {

    public MatsuTribeBirdstalker() {
        addTrigger(Triggers.dealsCombatDamageToCreature(), SequenceEffect.of(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                new SkipNextUntapEffect(TapUntapScope.TARGET)));

        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new GrantKeywordEffect(Keyword.REACH, GrantScope.SELF)),
                "{G}: This creature gains reach until end of turn."));
    }
}
