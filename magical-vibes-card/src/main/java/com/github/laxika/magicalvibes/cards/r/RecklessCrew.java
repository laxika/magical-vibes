package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAndAttachEquipmentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "146")
public class RecklessCrew extends Card {

    public RecklessCrew() {
        PermanentCount vehicles = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE), CountScope.CONTROLLER);
        PermanentCount equipment = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT), CountScope.CONTROLLER);
        CreateTokenEffect token = new CreateTokenEffect(
                new Sum(vehicles, equipment), "Dwarf Berserker", 2, 1, CardColor.RED,
                List.of(CardSubtype.DWARF, CardSubtype.BERSERKER), Set.of(), Set.of());
        addEffect(EffectSlot.SPELL, new CreateTokensAndAttachEquipmentEffect(token));
    }
}
