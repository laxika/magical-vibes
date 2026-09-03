package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlassesOfUrza.class, GrizzlyBears.class})
class GlassesOfUrzaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating taps the glasses and puts the ability on the stack")
    void activatingTapsAndPutsOnStack() {
        Permanent glasses = addReadyGlasses(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(glasses.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Ability looks at target player's hand")
    void looksAtTargetHand() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        addReadyGlasses(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"))
                .noneMatch(log -> log.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Ability against empty hand logs that hand is empty")
    void emptyHandLogged() {
        harness.setHand(player2, List.of());
        addReadyGlasses(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("empty"));
    }

    @Test
    @DisplayName("Can target self to look at own hand")
    void canTargetSelf() {
        addReadyGlasses(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    @Test
    @DisplayName("Can activate immediately from a newly entered noncreature artifact")
    void noncreatureArtifactIgnoresSummoningSickness() {
        Permanent glasses = harness.addToBattlefieldAndReturn(player1, new GlassesOfUrza());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(glasses.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent glasses = addReadyGlasses(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
        assertThat(glasses.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent glasses = addReadyGlasses(player1);
        glasses.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyGlasses(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GlassesOfUrza());
        perm.setSummoningSick(false);
        return perm;
    }
}
