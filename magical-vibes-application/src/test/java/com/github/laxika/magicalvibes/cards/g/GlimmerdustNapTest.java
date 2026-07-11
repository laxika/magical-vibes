package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlimmerdustNapTest extends BaseCardTest {

    // ===== Targeting restriction: enchant tapped creature =====

    @Test
    @DisplayName("Can target a tapped creature")
    void canTargetTappedCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new GlimmerdustNap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent untapped = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(untapped);

        // A legal tapped target exists too, so the card is playable and the
        // rejection reports the target restriction rather than "not playable".
        Permanent tapped = new Permanent(new GrizzlyBears());
        tapped.tap();
        gd.playerBattlefields.get(player2.getId()).add(tapped);

        harness.setHand(player1, List.of(new GlimmerdustNap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, untapped.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    // ===== Resolving attaches =====

    @Test
    @DisplayName("Resolving attaches Glimmerdust Nap to the tapped creature")
    void resolvingAttachesToTarget() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new GlimmerdustNap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glimmerdust Nap")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    // ===== Prevents untapping =====

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent nap = new Permanent(new GlimmerdustNap());
        nap.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(nap);

        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature untaps again after Glimmerdust Nap is removed")
    void creatureUntapsAfterRemoval() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent nap = new Permanent(new GlimmerdustNap());
        nap.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(nap);

        gd.playerBattlefields.get(player1.getId()).remove(nap);

        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
