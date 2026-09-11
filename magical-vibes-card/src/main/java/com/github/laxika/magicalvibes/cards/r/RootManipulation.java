package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "SOS", collectorNumber = "222")
public class RootManipulation extends Card {

    public RootManipulation() {
        // Until end of turn, creatures you control get +2/+2 and gain menace.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 2));
        addEffect(EffectSlot.SPELL,
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.OWN_CREATURES));

        // Until end of turn, creatures you control gain
        // "Whenever this creature attacks, you gain 1 life."
        addEffect(EffectSlot.SPELL, new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                EffectSlot.ON_ATTACK, new GainLifeEffect(1)));
    }
}
