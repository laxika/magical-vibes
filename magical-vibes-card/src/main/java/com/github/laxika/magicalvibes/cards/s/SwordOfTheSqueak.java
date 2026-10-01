package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBasePowerEqualsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBaseToughnessEqualsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "40")
@CardRegistration(set = "BLC", collectorNumber = "72")
public class SwordOfTheSqueak extends Card {

    public SwordOfTheSqueak() {
        PermanentAnyOfPredicate basePowerOrToughnessOne = new PermanentAnyOfPredicate(List.of(
                new PermanentBasePowerEqualsPredicate(1),
                new PermanentBaseToughnessEqualsPredicate(1)));
        PermanentCount qualifyingCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        basePowerOrToughnessOne)),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                qualifyingCreatures, qualifyingCreatures, GrantScope.EQUIPPED_CREATURE));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.HAMSTER),
                                new CardSubtypePredicate(CardSubtype.MOUSE),
                                new CardSubtypePredicate(CardSubtype.RAT),
                                new CardSubtypePredicate(CardSubtype.SQUIRREL))),
                        new AttachSourceEquipmentToEnteringCreatureEffect()));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
