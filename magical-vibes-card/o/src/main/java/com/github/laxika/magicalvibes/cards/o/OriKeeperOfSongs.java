package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerHasEnduringStory;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.StoriedEffect;

import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "23")
public class OriKeeperOfSongs extends Card {

    public OriKeeperOfSongs() {
        addEffect(EffectSlot.STATIC, new StoriedEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHasEnduringStory(),
                new StaticBoostEffect(1, 0, Set.of(Keyword.VIGILANCE), GrantScope.SELF)));
    }
}
