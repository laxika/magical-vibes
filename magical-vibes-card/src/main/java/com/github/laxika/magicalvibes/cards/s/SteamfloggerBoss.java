package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "121")
public class SteamfloggerBoss extends Card {

    public SteamfloggerBoss() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, Set.of(Keyword.HASTE), GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.RIGGER)));
    }
}
