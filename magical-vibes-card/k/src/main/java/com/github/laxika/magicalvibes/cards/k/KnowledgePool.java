package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolCastTriggerEffect;

@CardRegistration(set = "MBS", collectorNumber = "111")
public class KnowledgePool extends Card {

    public KnowledgePool() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTopCardsToSourceEffect(3, false, false, LibraryScope.EACH_PLAYER));
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new KnowledgePoolCastTriggerEffect());
    }
}
