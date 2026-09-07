package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
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

@CardUsed({RockyRebuke.class, GrizzlyBears.class, LlanowarElves.class, AirElemental.class})
class RockyRebukeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the source creature's power without damaging it")
    void dealsSourcePowerDamageOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        castRockyRebuke("Grizzly Bears", "Air Elemental");

        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(source.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Lethal power damage destroys the opponent's creature")
    void destroysOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        castRockyRebuke("Grizzly Bears", "Llanowar Elves");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the source")
    void cannotTargetOpponentCreatureAsSource() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new RockyRebuke()));
        addMana();

        UUID opponentSourceId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID opponentTargetId = harness.getPermanentId(player2, "Air Elemental");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentSourceId, opponentTargetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target your own creature as the victim")
    void cannotTargetOwnCreatureAsVictim() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new RockyRebuke()));
        addMana();

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID victimId = harness.getPermanentId(player1, "Llanowar Elves");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, victimId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Deals no damage if the source leaves before resolution")
    void dealsNoDamageIfSourceLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new RockyRebuke()));
        addMana();

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, List.of(sourceId, targetId));

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getMarkedDamage()).isZero();
    }

    private void castRockyRebuke(String sourceName, String targetName) {
        harness.setHand(player1, List.of(new RockyRebuke()));
        addMana();

        UUID sourceId = harness.getPermanentId(player1, sourceName);
        UUID targetId = harness.getPermanentId(player2, targetName);
        harness.castInstant(player1, 0, List.of(sourceId, targetId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
