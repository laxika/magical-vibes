package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Dreamcatcher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaboonSpirit.class, Dreamcatcher.class, GrizzlyBears.class})
class BaboonSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken Spirit entering creates a restricted Spirit token")
    void anotherSpiritCreatesToken() {
        addReadyBaboon(player1);
        castDreamcatcher();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("The created Spirit cannot block or be blocked by non-Spirits")
    void createdSpiritCannotInteractWithNonSpiritsInCombat() {
        addReadyBaboon(player1);
        castDreamcatcher();
        Permanent token = findToken(player1);
        token.setSummoningSick(false);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        token.setAttacking(true);
        prepareDeclareBlockers(player1);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(bears),
                gd.playerBattlefields.get(player1.getId()).indexOf(token)))))
                .isInstanceOf(IllegalStateException.class);

        token.setAttacking(false);
        bears.setAttacking(true);
        prepareDeclareBlockers(player2);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(token),
                gd.playerBattlefields.get(player2.getId()).indexOf(bears)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability returns another creature at the next end step")
    void flickersAnotherCreatureUntilNextEndStep() {
        addReadyBaboon(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(PendingExileReturn.class))
                .anyMatch(action -> action.card().getName().equals("Grizzly Bears"));

        advanceToNextEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The activated ability only targets another creature you control")
    void rejectsIllegalTargets() {
        Permanent baboon = addReadyBaboon(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, baboon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBaboon(Player player) {
        return addCreatureReady(player, new BaboonSpirit());
    }

    private void castDreamcatcher() {
        harness.setHand(player1, List.of(new Dreamcatcher()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    private void advanceToNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
