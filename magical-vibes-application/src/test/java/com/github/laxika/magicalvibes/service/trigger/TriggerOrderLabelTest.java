package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The labels of the prompt that asks a player to order the triggered abilities they control that triggered together
 * (CR 603.3b).
 */
@CardUsed({GrizzlyBears.class})
class TriggerOrderLabelTest extends BaseCardTest {

    private static Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private List<String> orderOptionsAfterAttackingWith(Card card) {
        harness.setAutoOrderTriggers(player1, false);
        addCreatureReady(player1, card);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, List.of(0)));
        PendingInteraction.ColorChoice prompt =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(prompt).isNotNull();
        return prompt.options();
    }

    @Test
    @DisplayName("A boost of other creatures, which targets nothing, is not labelled a target creature's")
    void untargetedBoostIsNotLabelledTargetCreature() {
        Card card = creature("Rallying Drummer");
        card.addTrigger(Triggers.youAttack(),
                BoostEffect.each(Subjects.except(Subjects.otherCreatures(), Subjects.source()), 1, 1));
        card.addTrigger(Triggers.youAttack(), new GainLifeEffect(1));

        assertThat(orderOptionsAfterAttackingWith(card)).noneMatch(option -> option.contains("target creature"));
    }
}
