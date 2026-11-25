package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "152")
public class SeasonOfTheBold extends Card {

    public SeasonOfTheBold() {
        addSpellEffect(ChooseOneEffect.budgetedModes(List.of(
                new ChooseOneEffect.ChooseOneOption("Create a tapped Treasure token",
                        CreateTokenEffect.ofTreasureToken(1, true)),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top two cards of your library. Until the end of your next turn, you may play them.",
                        new ExileTopCardMayPlayThisTurnEffect(2, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)),
                new ChooseOneEffect.ChooseOneOption(
                        "Until the end of your next turn, whenever you cast a spell, deal 2 damage to up to one target creature.",
                        new RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect(
                                new TriggeredAbility(Triggers.youCastSpell(),
                                SequenceEffect.upToOneTarget(DealDamageEffect.toTargetCreature(2))),
                                TargetFilters.creature()))
        ), List.of(1, 2, 3), 5));
    }
}
