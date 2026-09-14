package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvatarOfHope.class, GrizzlyBears.class})
class AvatarOfHopeTest extends BaseCardTest {

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
    @DisplayName("An opponent's low life total does not enable Avatar of Hope's cost reduction")
    void opponentLifeDoesNotEnableReduction() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 3);
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

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Avatar of Hope")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new AvatarOfHope());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Avatar of Hope can block four attackers at once")
    void canBlockFourAttackers() {
        Permanent avatarPerm = addCreatureReady(player2, new AvatarOfHope());

        for (int i = 0; i < 4; i++) {
            Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2),
                new BlockerAssignment(0, 3)
        ));

        assertThat(avatarPerm.isBlocking()).isTrue();
        assertThat(avatarPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2, 3);
    }

    @Test
    @DisplayName("A normal creature cannot block more than one attacker")
    void normalCreatureCannotBlockMultipleAttackers() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        for (int i = 0; i < 2; i++) {
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            attacker.setAttacking(true);
        }

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assigned too many times");
        assertThat(blocker.isBlocking()).isFalse();
    }
}
