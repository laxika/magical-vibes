package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureAndCreateSpiritTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "192")
public class HofriGhostforge extends Card {

    public HofriGhostforge() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1,
                Set.of(Keyword.TRAMPLE, Keyword.HASTE), GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));

        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new ExileDyingCreatureAndCreateSpiritTokenCopyEffect());
    }
}
