package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.Extortion;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritualFocus.class, SpectersWail.class, FreshVolunteers.class, Extortion.class,
        Scandalmonger.class})
class SpiritualFocusTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent-caused discard gains 2 life and offers a draw")
    void opponentCausedDiscardGainsLifeAndMayDraw() {
        harness.addToBattlefield(player1, new SpiritualFocus());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.setLibrary(player1, List.of(new FreshVolunteers()));

        harness.setHand(player2, List.of(new SpectersWail()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the draw still gains 2 life")
    void decliningDrawStillGainsLife() {
        harness.addToBattlefield(player1, new SpiritualFocus());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FreshVolunteers()));

        harness.setHand(player2, List.of(new SpectersWail()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(0);
    }

    @Test
    @DisplayName("A self-caused discard does not trigger Spiritual Focus")
    void selfCausedDiscardDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpiritualFocus());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SpectersWail(), new FreshVolunteers()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent-controlled ability that causes a discard triggers Spiritual Focus")
    void opponentCausedDiscardAbilityTriggers() {
        harness.addToBattlefield(player1, new SpiritualFocus());
        harness.setLife(player1, 20);
        FreshVolunteers discarded = new FreshVolunteers();
        harness.setHand(player1, List.of(discarded));
        harness.addToBattlefieldAndReturn(player2, new Scandalmonger());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A spell causing two discards triggers once for each discarded card")
    void triggersForEachDiscardedCard() {
        harness.addToBattlefield(player1, new SpiritualFocus());
        harness.setLife(player1, 20);
        FreshVolunteers first = new FreshVolunteers();
        FreshVolunteers second = new FreshVolunteers();
        harness.setHand(player1, List.of(first, second));
        harness.setHand(player2, List.of(new Extortion()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
