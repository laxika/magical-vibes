package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "17")
@CardRegistration(set = "FIC", collectorNumber = "105")
public class DancersChakrams extends Card {

    public DancersChakrams() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LivingWeaponEffect(new CreateTokenEffect("Hero", 1, 1, null,
                        List.of(CardSubtype.HERO), Set.of(), Set.of())));

        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(2, 2, Set.of(Keyword.LIFELINK), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantSubtypeEffect(CardSubtype.PERFORMER, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(2, 2, Set.of(Keyword.LIFELINK), GrantScope.OWN_PERMANENTS,
                        new PermanentIsCommanderPredicate()));

        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
