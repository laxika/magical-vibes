package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "40")
public class IvoryGuardians extends Card {

    public IvoryGuardians() {
        // Protection from red.
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.RED)));

        // Creatures named Ivory Guardians get +1/+1 as long as an opponent controls a nontoken red permanent.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentColorInPredicate(Set.of(CardColor.RED)),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())))),
                new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                        new PermanentNamedPredicate("Ivory Guardians"))));
    }
}
