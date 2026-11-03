package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YTDM", collectorNumber = "10")
public class DragonbloodTwins extends Card {

    public DragonbloodTwins() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new SetBasePowerToughnessEffect(4, 4, GrantScope.SELF),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
