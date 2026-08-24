package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurningProphet.class, Divination.class, GrizzlyBears.class, Shock.class})
class BurningProphetTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell pumps Burning Prophet and triggers scry 1")
    void noncreatureSpellPumpsAndScries() {
        Permanent prophet = addProphet();
        int initialPower = prophet.getEffectivePower();
        int initialToughness = prophet.getEffectiveToughness();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prophet)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, prophet)).isEqualTo(initialToughness);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Burning Prophet")
    void creatureSpellDoesNotTrigger() {
        Permanent prophet = addProphet();
        int initialPower = prophet.getEffectivePower();
        int initialToughness = prophet.getEffectiveToughness();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prophet)).isEqualTo(initialPower);
        assertThat(gqs.getEffectiveToughness(gd, prophet)).isEqualTo(initialToughness);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("The power boost wears off at end of turn")
    void powerBoostWearsOffAtEndOfTurn() {
        Permanent prophet = addProphet();
        int initialPower = prophet.getEffectivePower();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prophet)).isEqualTo(initialPower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prophet)).isEqualTo(initialPower);
    }

    private Permanent addProphet() {
        Permanent prophet = harness.addToBattlefieldAndReturn(player1, new BurningProphet());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return prophet;
    }
}
