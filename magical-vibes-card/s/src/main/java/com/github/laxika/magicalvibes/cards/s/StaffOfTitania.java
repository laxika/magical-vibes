package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRC", collectorNumber = "27")
@CardRegistration(set = "BRC", collectorNumber = "50")
public class StaffOfTitania extends Card {

    public StaffOfTitania() {
        PermanentCount forests = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                forests, forests, GrantScope.EQUIPPED_CREATURE));

        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                1, "Forest Dryad", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.FOREST, CardSubtype.DRYAD), Set.of(), Set.of(CardType.LAND)));

        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
