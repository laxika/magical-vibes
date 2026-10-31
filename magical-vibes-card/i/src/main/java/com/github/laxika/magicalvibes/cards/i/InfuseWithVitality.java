package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "194")
public class InfuseWithVitality extends Card {

    public InfuseWithVitality() {
        // Until end of turn, target creature gains deathtouch and "When this creature dies, return
        // it to the battlefield tapped under its owner's control." You gain 2 life.
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET))
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_DEATH, new ReturnSourceCardFromGraveyardToBattlefieldEffect(true)))
                .addSpellEffect(new GainLifeEffect(2));
    }
}
