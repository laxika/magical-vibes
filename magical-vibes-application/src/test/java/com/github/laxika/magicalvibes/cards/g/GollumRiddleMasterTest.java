package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GollumRiddleMaster.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class GollumRiddleMasterTest extends BaseCardTest {

    private static final String COUNTER = "Put a +1/+1 counter on Gollum";
    private static final String LIFE = "Each opponent loses 2 life and you gain 2 life";
    private static final String DRAW = "Draw a card";

    @Test
    @DisplayName("As Gollum enters, an odd/even choice is required")
    void enteringRequiresParityChoice() {
        harness.setHand(player1, List.of(new GollumRiddleMaster()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ODD");

        assertThat(findPermanent(player1, "Gollum, Riddle Master").getChosenManaValueParity())
                .isEqualTo(ManaValueParity.ODD);
    }

    @Test
    @DisplayName("An opponent's spell with the chosen parity triggers Gollum's counter mode")
    void matchingSpellPutsCounterOnGollum() {
        Permanent gollum = castGollum(ManaValueParity.ODD);
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.handleListChoice(player1, COUNTER);
        harness.passBothPriorities();

        assertThat(gollum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell with the other parity does not trigger Gollum")
    void nonmatchingSpellDoesNotTrigger() {
        Permanent gollum = castGollum(ManaValueParity.ODD);
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gollum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The life mode drains each opponent and gains life")
    void lifeMode() {
        castGollum(ManaValueParity.EVEN);
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.handleListChoice(player1, LIFE);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The draw mode draws one card")
    void drawMode() {
        harness.setLibrary(player1, List.of(new Forest()));
        castGollum(ManaValueParity.ODD);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("A chosen mode is not offered again")
    void chosenModeIsConsumed() {
        castGollum(ManaValueParity.ODD);
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new LlanowarElves(), new LlanowarElves()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .doesNotContain(DRAW);
    }

    private Permanent castGollum(ManaValueParity parity) {
        harness.setHand(player1, List.of(new GollumRiddleMaster()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, parity.name());
        harness.passBothPriorities();
        return findPermanent(player1, "Gollum, Riddle Master");
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
