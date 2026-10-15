package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "187")
public class TheLonelyMountain extends Card {

    public TheLonelyMountain() {
        PermanentHasSubtypePredicate equipment = new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT);
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new NotCondition(new ControlsPermanentCount(1, equipment)), new EntersTappedEffect()));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        PermanentCount equipmentYouControl = new PermanentCount(equipment, CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{R}",
                List.of(
                        new ReduceActivationCostEffect(equipmentYouControl),
                        new CreateTokenEffect("Dwarf", 2, 2, CardColor.RED,
                                List.of(CardSubtype.DWARF), Set.of(), Set.of())
                ),
                "{4}{R}, {T}: Create a 2/2 red Dwarf creature token. This ability costs {1} less to activate "
                        + "for each Equipment you control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
