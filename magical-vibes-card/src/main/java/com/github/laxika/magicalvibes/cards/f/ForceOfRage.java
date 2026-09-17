package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ScheduleCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "124")
public class ForceOfRage extends Card {

    public ForceOfRage() {
        // If it's not your turn, you may exile a red card from your hand rather than pay this
        // spell's mana cost.
        addCastingOption(new AlternateHandCast(
                List.of(new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.RED), "red")),
                new NotControllerTurn(),
                false));

        // Create two 3/1 red Elemental creature tokens with trample and haste.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2,
                "Elemental",
                3,
                1,
                CardColor.RED,
                List.of(CardSubtype.ELEMENTAL),
                Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                Set.of()));

        // Sacrifice those tokens at the beginning of your next upkeep.
        addEffect(EffectSlot.SPELL, ScheduleCreatedPermanentsEffect.forController(
                DelayedPermanentActionKind.SACRIFICE_AT_NEXT_UPKEEP));
    }
}
