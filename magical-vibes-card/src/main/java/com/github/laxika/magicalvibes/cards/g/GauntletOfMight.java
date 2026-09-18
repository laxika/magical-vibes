package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "2ED", collectorNumber = "245")
public class GauntletOfMight extends Card {

    public GauntletOfMight() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.RED))));
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddManaWhenLandOfSubtypeTappedForManaEffect(CardSubtype.MOUNTAIN, ManaColor.RED));
    }
}
