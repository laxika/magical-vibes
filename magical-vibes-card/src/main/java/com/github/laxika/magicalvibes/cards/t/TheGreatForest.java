package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "OHOP", collectorNumber = "14")
public class TheGreatForest extends Card {

    public TheGreatForest() {
        addEffect(EffectSlot.STATIC, new AssignCombatDamageWithToughnessEffect(GrantScope.ALL_CREATURES));
        addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                new BoostAllOwnCreaturesEffect(0, 2),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES)));
    }
}
