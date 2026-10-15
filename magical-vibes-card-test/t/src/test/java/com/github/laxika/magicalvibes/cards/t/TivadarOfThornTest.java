package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.g.GoblinSkycutter;
import com.github.laxika.magicalvibes.cards.s.Snapback;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TivadarOfThorn.class, GoblinSkycutter.class, AshcoatBear.class, SuddenShock.class, Snapback.class})
class TivadarOfThornTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target Goblin")
    void etbDestroysTargetGoblin() {
        harness.addToBattlefield(player2, new GoblinSkycutter());
        harness.setHand(player1, List.of(new TivadarOfThorn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Goblin Skycutter");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tivadar of Thorn");
        harness.assertNotOnBattlefield(player2, "Goblin Skycutter");
        harness.assertInGraveyard(player2, "Goblin Skycutter");
    }

    @Test
    @DisplayName("Protection from red prevents red spells from targeting Tivadar")
    void protectionFromRedPreventsRedSpellTargeting() {
        harness.addToBattlefield(player2, new TivadarOfThorn());
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Tivadar of Thorn");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("First strike lets Tivadar survive combat with a 2/2")
    void firstStrikeDealsDamageBeforeAshcoatBear() {
        Permanent attacker = addCreatureReady(player1, new AshcoatBear());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new TivadarOfThorn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Ashcoat Bear");
        harness.assertOnBattlefield(player2, "Tivadar of Thorn");
    }

    @Test
    @DisplayName("Cannot target a non-Goblin")
    void cannotTargetNonGoblin() {
        harness.addToBattlefield(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new TivadarOfThorn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Ashcoat Bear");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Goblin");
    }

    @Test
    @DisplayName("ETB has no effect without a Goblin target")
    void etbHasNoEffectWithoutTarget() {
        harness.setHand(player1, List.of(new TivadarOfThorn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tivadar of Thorn");
    }

    @Test
    void etbDestroysOwnGoblin() {
        harness.addToBattlefield(player1, new GoblinSkycutter());
        harness.setHand(player1, List.of(new TivadarOfThorn()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, harness.getPermanentId(player1, "Goblin Skycutter"));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tivadar of Thorn");
        harness.assertNotOnBattlefield(player1, "Goblin Skycutter");
        harness.assertInGraveyard(player1, "Goblin Skycutter");
    }

    @Test
    void etbStillDestroysGoblinAfterTivadarLeaves() {
        harness.addToBattlefield(player2, new GoblinSkycutter());
        harness.setHand(player1, List.of(new TivadarOfThorn(), new Snapback()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Goblin Skycutter"));
        harness.passBothPriorities();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Tivadar of Thorn"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tivadar of Thorn");
        harness.assertInHand(player1, "Tivadar of Thorn");
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Goblin Skycutter");
        harness.assertNotOnBattlefield(player2, "Goblin Skycutter");
    }

    @Test
    void etbDoesNotDestroyGoblinThatLeftTheBattlefield() {
        harness.addToBattlefield(player2, new GoblinSkycutter());
        harness.setHand(player1, List.of(new TivadarOfThorn(), new Snapback()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Goblin Skycutter");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tivadar of Thorn");
        harness.assertInHand(player2, "Goblin Skycutter");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
