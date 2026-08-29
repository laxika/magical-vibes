package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseGameAtEndStepEffect;

@CardRegistration(set = "GRN", collectorNumber = "159")
public class ChanceForGlory extends Card {

    public ChanceForGlory() {
        // "Creatures you control gain indestructible."
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES));
        // "Take an extra turn after this one."
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
        // "At the beginning of that turn's end step, you lose the game."
        addEffect(EffectSlot.SPELL, new RegisterLoseGameAtEndStepEffect());
    }
}
