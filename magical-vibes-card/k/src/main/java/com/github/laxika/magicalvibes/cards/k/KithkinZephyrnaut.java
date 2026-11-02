package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "16")
public class KithkinZephyrnaut extends Card {

    public KithkinZephyrnaut() {
        // Kinship — At the beginning of your upkeep, you may look at the top card of your library.
        // If it shares a creature type with this creature, you may reveal it. If you do, this creature
        // gets +2/+2 and gains flying and vigilance until end of turn.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new KinshipEffect(List.of(
                new BoostSelfEffect(2, 2),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF))));
    }
}
