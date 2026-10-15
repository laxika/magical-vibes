package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

@CardRegistration(set = "VIS", collectorNumber = "97")
public class TalruumChampion extends Card {

    public TalruumChampion() {
        // Whenever this creature blocks or becomes blocked by a creature, that creature loses
        // first strike until end of turn. The combat opponent is carried as the trigger's
        // (non-targeting) target.
        addEffect(EffectSlot.ON_BLOCK, new RemoveKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new RemoveKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET),
                TriggerMode.PER_BLOCKER);
    }
}
