package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCardTypeFromAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "151")
@CardRegistration(set = "SLD", collectorNumber = "78")
@CardRegistration(set = "PIO", collectorNumber = "313")
@CardRegistration(set = "DSC", collectorNumber = "89")
public class MogisGodOfSlaughter extends Card {

    public MogisGodOfSlaughter() {
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new DevotionToColorsAtLeast(
                        Set.of(ManaColor.BLACK, ManaColor.RED), 7)),
                new RemoveCardTypeFromAttachedPermanentEffect(CardType.CREATURE, Subjects.source())));
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                UnlessPaysEffect.unlessPays(Subjects.activePlayer(), new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "creature", false),
                        DealDamageEffect.to(Subjects.activePlayer(), 2)));
    }
}
