package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlassesOfUrzaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating taps the glasses and puts the ability on the stack")
    void activatingTapsAndPutsOnStack() {
        Permanent glasses = addReadyGlasses(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(glasses.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Ability looks at target player's hand")
    void looksAtTargetHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new GlassesOfUrza())));
        addReadyGlasses(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Glasses of Urza"));
    }

    @Test
    @DisplayName("Ability against empty hand logs that hand is empty")
    void emptyHandLogged() {
        harness.setHand(player2, new ArrayList<>());
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
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent glasses = addReadyGlasses(player1);
        glasses.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyGlasses(Player player) {
        GlassesOfUrza card = new GlassesOfUrza();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
