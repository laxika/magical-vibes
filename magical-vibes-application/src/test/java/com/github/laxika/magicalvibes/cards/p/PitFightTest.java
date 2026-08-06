package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PitFightTest extends BaseCardTest {

    @Test
    @DisplayName("Creature you control fights an opponent's creature")
    void fightKillsSmallerCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new PitFight()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(giantId, elvesId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Second target may be another creature you control")
    void secondTargetMayBeOwnCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new PitFight()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(giantId, elvesId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Both creatures die when they deal each other lethal damage")
    void fightKillsBothCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PitFight()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID myBearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID theirBearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(myBearId, theirBearId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new PitFight()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID theirBearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID theirElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(theirBearId, theirElvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot pick the same creature for both targets")
    void cannotPickSameCreatureTwice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PitFight()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, bearId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither creature deals damage when the second target is removed before resolution")
    void neitherFightsWhenSecondTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new PitFight()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, elvesId));

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getMarkedDamage()).isZero();
    }
}
