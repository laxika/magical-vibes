package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwiftResponse.class, AlpineWatchdog.class, Forest.class})
class SwiftResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target tapped creature")
    void destroysTappedCreature() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        tappedCreature.tap();

        castSwiftResponse(tappedCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpine Watchdog");
        harness.assertInGraveyard(player2, "Alpine Watchdog");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());

        harness.setHand(player1, List.of(new SwiftResponse()));
        addSwiftResponseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature")
    void cannotTargetTappedNoncreature() {
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        tappedLand.tap();

        harness.setHand(player1, List.of(new SwiftResponse()));
        addSwiftResponseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, tappedLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Fizzles if the target becomes untapped before resolution")
    void fizzlesIfTargetBecomesUntapped() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        tappedCreature.tap();

        castSwiftResponse(tappedCreature.getId());
        tappedCreature.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tappedCreature);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Swift Response");
    }

    @Test
    @DisplayName("Can destroy your own tapped creature")
    void destroysOwnTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        creature.tap();

        castSwiftResponse(creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alpine Watchdog");
        harness.assertInGraveyard(player1, "Alpine Watchdog");
        harness.assertInGraveyard(player1, "Swift Response");
    }

    @Test
    @DisplayName("Destroys a target that untaps and taps again before resolution")
    void destroysTargetRetappedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        creature.tap();

        castSwiftResponse(creature.getId());
        creature.untap();
        creature.tap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpine Watchdog");
        harness.assertInGraveyard(player2, "Alpine Watchdog");
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
