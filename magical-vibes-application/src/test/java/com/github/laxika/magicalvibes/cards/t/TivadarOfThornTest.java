package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TivadarOfThorn.class, GoblinPiker.class, GrizzlyBears.class, Shock.class})
class TivadarOfThornTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target Goblin")
    void etbDestroysTargetGoblin() {
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.setHand(player1, List.of(new TivadarOfThorn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Goblin Piker");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tivadar of Thorn");
        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        harness.assertInGraveyard(player2, "Goblin Piker");
    }

    @Test
    @DisplayName("Protection from red prevents red spells from targeting Tivadar")
    void protectionFromRedPreventsRedSpellTargeting() {
        harness.addToBattlefield(player2, new TivadarOfThorn());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Tivadar of Thorn");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("First strike lets Tivadar survive combat with a 2/2")
    void firstStrikeDealsDamageBeforeGrizzlyBears() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new TivadarOfThorn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Tivadar of Thorn");
    }

    @Test
    @DisplayName("Cannot target a non-Goblin")
    void cannotTargetNonGoblin() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TivadarOfThorn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Goblin");
    }

    @Test
    @DisplayName("ETB has no effect without a Goblin target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new TivadarOfThorn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tivadar of Thorn");
    }
}
