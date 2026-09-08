package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpelTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target tapped creature")
    void exilesTargetTappedCreature() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedCreature);

        castExpel(tappedCreature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        addTappedValidTarget(player1);
        Permanent untappedCreature = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(untappedCreature);

        harness.setHand(player1, List.of(new Expel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature")
    void cannotTargetTappedNoncreature() {
        addTappedValidTarget(player1);
        Permanent tappedLand = new Permanent(new Forest());
        tappedLand.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedLand);

        harness.setHand(player1, List.of(new Expel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, tappedLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Fizzles if the target becomes untapped before resolution")
    void fizzlesIfTargetBecomesUntappedBeforeResolution() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedCreature);

        castExpel(tappedCreature);
        tappedCreature.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    private void castExpel(Permanent target) {
        harness.setHand(player1, List.of(new Expel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
    }

    private void addTappedValidTarget(com.github.laxika.magicalvibes.model.Player player) {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player.getId()).add(tappedCreature);
    }
}
