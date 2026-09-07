package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "151")
public class GoblinTombRaider extends Card {

    public GoblinTombRaider() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentIsArtifactPredicate()),
                new StaticBoostEffect(1, 0, Set.of(Keyword.HASTE), GrantScope.SELF)));
    }
}
