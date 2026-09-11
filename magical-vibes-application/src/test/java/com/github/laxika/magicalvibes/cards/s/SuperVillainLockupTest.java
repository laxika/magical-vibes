package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuperVillainLockup.class, GrizzlyBears.class, Naturalize.class})
class SuperVillainLockupTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and exiles a tapped creature an opponent controls")
    void exilesTappedOpponentCreature() {
        Permanent target = addTappedCreature(player2);
        castAndResolve(target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareLockup();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Cannot target a tapped creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent target = addTappedCreature(player1);
        prepareLockup();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Target must still be tapped when the enter-the-battlefield ability resolves")
    void fizzlesIfTargetBecomesUntappedBeforeResolution() {
        Permanent target = addTappedCreature(player2);
        prepareLockup();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Exiled creature returns when Super Villain Lockup leaves")
    void exiledCreatureReturnsWhenLockupLeaves() {
        Permanent target = addTappedCreature(player2);
        castAndResolve(target.getId());

        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        UUID lockupId = harness.getPermanentId(player1, "Super Villain Lockup");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lockupId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    private Permanent addTappedCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.tap();
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void prepareLockup() {
        harness.setHand(player1, List.of(new SuperVillainLockup()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castAndResolve(UUID targetId) {
        prepareLockup();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
