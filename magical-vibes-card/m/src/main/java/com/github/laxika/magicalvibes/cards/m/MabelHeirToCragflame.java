package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "224")
public class MabelHeirToCragflame extends Card {

    public MabelHeirToCragflame() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MOUSE))));

        List<Ability> cragflameEffects = List.of(
                StaticAbility.of(new StaticBoostEffect(1, 1,
                        Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.HASTE),
                        GrantScope.EQUIPPED_CREATURE)));
        CreateTokenEffect cragflame = new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Cragflame", 0, 0,
                null, null, List.of(CardSubtype.EQUIPMENT), Set.of(), Set.of(),
                false, false, cragflameEffects, List.of(new EquipActivatedAbility("{2}")),
                false, false, true, 0, Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, cragflame);
    }
}
