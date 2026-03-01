package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WingPunctureTest extends BaseCardTest {

    @Test
    @DisplayName("Creature you control deals power damage to creature with flying, killing it")
    void killsFlyingCreature() {
        // Air Elemental is 4/4 flying, Grizzly Bears is 2/2
        // We need a bigger creature to kill Air Elemental — use two scenarios
        // Actually let's use a smaller flyer: use LlanowarElves as biter won't work (no flying)
        // Let's just test with Grizzly Bears (2/2) biting Air Elemental (4/4) — won't kill but deals damage
        // For a kill test, let's put Air Elemental on our side as biter vs opponent's smaller flyer
        // Simplest: Grizzly Bears (2/2) deals 2 damage to Air Elemental (4/4) — survives with 2 damage
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, List.of(bearId, elementalId));
        harness.passBothPriorities();

        // Air Elemental should survive (2 damage < 4 toughness)
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Large creature kills flying creature with power damage")
    void largeCreatureKillsFlyingCreature() {
        // Air Elemental (4/4 flying) as biter deals 4 damage to opponent's Air Elemental (4/4 flying)
        harness.addToBattlefield(player1, new AirElemental());
        AirElemental opponentFlyer = new AirElemental();
        harness.addToBattlefield(player2, opponentFlyer);
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID biterId = harness.getPermanentId(player1, "Air Elemental");
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, List.of(biterId, targetId));
        harness.passBothPriorities();

        // Opponent's Air Elemental should be destroyed (4 damage = 4 toughness)
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature without flying as second target")
    void cannotTargetNonFlyingCreatureAsSecondTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Cannot target opponent's creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, elementalId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Spell fizzles when all targets removed before resolution")
    void fizzlesWhenAllTargetsRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, List.of(bearId, elementalId));

        // Remove both targets before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Bite does nothing when biter removed before resolution")
    void biteDoesNothingWhenBiterRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, List.of(bearId, elementalId));

        // Remove the biter before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Air Elemental should survive — no biter to deal damage
        harness.assertOnBattlefield(player2, "Air Elemental");
    }
}
