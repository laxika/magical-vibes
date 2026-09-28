package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "17")
@CardRegistration(set = "TDC", collectorNumber = "57")
public class WillOfTheMardu extends Card {

    public WillOfTheMardu() {
        PermanentCount targetPlayerCreatures = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.TARGET_PLAYER);
        PermanentCount creaturesYouControl = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a number of 1/1 red Warrior creature tokens equal to the number of creatures target player controls",
                        new CreateTokenEffect(targetPlayerCreatures, "Warrior", 1, 1,
                                CardColor.RED, List.of(CardSubtype.WARRIOR), Set.of(), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "Will of the Mardu deals damage to target creature equal to the number of creatures you control",
                        new DealDamageToTargetCreatureEffect(creaturesYouControl))
        ), new ControlledCommanderAsCast()));
    }
}
