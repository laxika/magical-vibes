package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwiftResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target tapped creature")
    void destroysTappedCreature() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        gd.playerBattlefields.get(player2.getId()).add(tappedCreature);

        castSwiftResponse(tappedCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent untappedCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(untappedCreature);

        harness.setHand(player1, List.of(new SwiftResponse()));
        addSwiftResponseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature")
    void cannotTargetTappedNoncreature() {
        Permanent tappedLand = new Permanent(new Forest());
        tappedLand.tap();
        gd.playerBattlefields.get(player2.getId()).add(tappedLand);

        harness.setHand(player1, List.of(new SwiftResponse()));
        addSwiftResponseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, tappedLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Fizzles if the target becomes untapped before resolution")
    void fizzlesIfTargetBecomesUntapped() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        gd.playerBattlefields.get(player2.getId()).add(tappedCreature);

        castSwiftResponse(tappedCreature.getId());
        tappedCreature.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tappedCreature);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Swift Response");
    }

    private void castSwiftResponse(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SwiftResponse()));
        addSwiftResponseMana();
        harness.castInstant(player1, 0, targetId);
    }

    private void addSwiftResponseMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
