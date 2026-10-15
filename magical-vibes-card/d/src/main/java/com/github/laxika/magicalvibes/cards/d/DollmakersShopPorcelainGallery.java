package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "4")
public class DollmakersShopPorcelainGallery extends Card {

    public DollmakersShopPorcelainGallery() {
        setRoomDoorManaCosts(List.of("{1}{W}", "{4}{W}{W}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Dollmaker's Shop", List.of())
                        .withManaCost("{1}{W}"),
                new ChooseOneEffect.ChooseOneOption("Porcelain Gallery", List.of())
                        .withManaCost("{4}{W}{W}")
        )));

        PermanentAllOfPredicate nonToyCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.TOY))));
        CreateTokenEffect toyToken = new CreateTokenEffect(
                1, "Toy", 1, 1, CardColor.WHITE, List.of(CardSubtype.TOY), Set.of(),
                Set.of(CardType.ARTIFACT));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(0),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                        new ConditionalEffect(new MinimumMatchingAttackers(1, nonToyCreature), toyToken),
                        GrantScope.SELF)));

        PermanentCount creaturesYouControl = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(1),
                new SetBasePowerToughnessToAmountEffect(
                        creaturesYouControl, creaturesYouControl, GrantScope.ALL_OWN_CREATURES)));
    }
}
