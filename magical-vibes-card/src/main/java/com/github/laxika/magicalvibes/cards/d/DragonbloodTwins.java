package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "10")
public class DragonbloodTwins extends Card {

    public DragonbloodTwins() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                2,
                List.of(
                        new SetBasePowerToughnessEffect(4, 4, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)
                )
        ));
    }
}
