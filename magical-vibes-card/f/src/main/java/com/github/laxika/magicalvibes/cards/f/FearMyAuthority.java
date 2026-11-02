package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AbandonSchemeUnlessDiscardOrPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "333")
public class FearMyAuthority extends Card {

    public FearMyAuthority() {
        addStaticEffect(new StaticBoostEffect(2, 2, Set.of(Keyword.FEAR), GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new AbandonSchemeUnlessDiscardOrPayLifeEffect(3));
    }
}
