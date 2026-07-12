package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarOfHopeTest extends BaseCardTest {

    // ===== Conditional cost reduction =====

    @Test
    @DisplayName("Cannot cast Avatar of Hope for {W}{W} at more than 3 life")
    void cannotCastWithReductionAboveThreeLife() {
        harness.setLife(player1, 4);
        harness.setHand(player1, List.of(new AvatarOfHope()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast Avatar of Hope for {W}{W} at 3 or less life")
    void canCastWithReductionAtThreeLife() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new AvatarOfHope()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Avatar of Hope");
    }

    // ===== Can block any number of creatures =====

    @Test
    @DisplayName("Avatar of Hope can block four attackers at once")
    void canBlockFourAttackers() {
        AvatarOfHope avatar = new AvatarOfHope();
        Permanent avatarPerm = new Permanent(avatar);
        avatarPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(avatarPerm);

        for (int i = 0; i < 4; i++) {
            GrizzlyBears atk = new GrizzlyBears();
            Permanent atkPerm = new Permanent(atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2),
                new BlockerAssignment(0, 3)
        ));

        assertThat(avatarPerm.isBlocking()).isTrue();
        assertThat(avatarPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2, 3);
    }
}
