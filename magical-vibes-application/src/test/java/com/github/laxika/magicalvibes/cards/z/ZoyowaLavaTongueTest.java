package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PanickedAltisaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZoyowaLavaTongue.class, PanickedAltisaur.class, Abrade.class, Mountain.class})
class ZoyowaLavaTongueTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when its controller has not descended")
    void doesNotTriggerWithoutDescended() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent may discard instead of taking damage")
    void opponentDiscards() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player1);
        harness.setHand(player2, List.of(new Abrade()));
        harness.setLibrary(player2, List.of());

        resolveTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent may sacrifice instead of taking damage")
    void opponentSacrifices() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player1);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player2, new PanickedAltisaur());

        resolveTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Panicked Altisaur");
    }

    @Test
    @DisplayName("An opponent who declines both options is dealt 3 damage")
    void opponentDeclinesBothOptions() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player1);
        harness.setHand(player2, List.of(new Abrade()));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player2, new PanickedAltisaur());

        resolveTrigger();

        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player2, "Panicked Altisaur");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent with no available option is dealt 3 damage")
    void opponentWithNoOptionTakesDamage() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player1);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());

        resolveTrigger();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's descent does not enable the trigger")
    void opponentsDescentDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player2);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player1);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Descending before Zoyowa enters still enables its end-step trigger")
    void descentBeforeEnteringEnablesTrigger() {
        descend(player1);
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());

        resolveTrigger();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Multiple descents produce only one end-step trigger")
    void multipleDescentsTriggerOnlyOnce() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player1);
        descend(player1);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Resolving a nonpermanent spell does not count as descending")
    void nonpermanentInGraveyardDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PanickedAltisaur());
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Abrade");
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The triggered ability deals damage even after Zoyowa leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent zoyowa = harness.addToBattlefieldAndReturn(player1, new ZoyowaLavaTongue());
        descend(player1);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, zoyowa));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An opponent may decline to discard and choose a land to sacrifice")
    void opponentChoosesLandAfterDecliningDiscard() {
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        descend(player1);
        harness.setHand(player2, List.of(new Abrade()));
        harness.setLibrary(player2, List.of());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PanickedAltisaur());

        resolveTrigger();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultiplePermanentsChosen(player2, List.of(land.getId()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Mountain");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature).doesNotContain(land);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Deathtouch destroys a blocker with more toughness than Zoyowa's power")
    void deathtouchDestroysLargerBlocker() {
        addCreatureReady(player1, new ZoyowaLavaTongue());
        harness.addToBattlefield(player2, new PanickedAltisaur());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Zoyowa Lava-Tongue");
        harness.assertInGraveyard(player2, "Panicked Altisaur");
        harness.assertNotOnBattlefield(player1, "Zoyowa Lava-Tongue");
        harness.assertNotOnBattlefield(player2, "Panicked Altisaur");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
    private void descend(Player player) {
        Permanent fodder = harness.addToBattlefieldAndReturn(player, new PanickedAltisaur());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, fodder));
    }

    private void resolveTrigger() {
        advanceToEndStep(player1);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
