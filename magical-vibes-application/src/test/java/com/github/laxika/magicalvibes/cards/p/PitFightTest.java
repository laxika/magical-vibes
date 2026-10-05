package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({PitFight.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
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
        harness.castAndResolveInstant(player1, 0, List.of(giantId, elvesId));

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
        harness.castAndResolveInstant(player1, 0, List.of(giantId, elvesId));

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
        harness.castAndResolveInstant(player1, 0, List.of(myBearId, theirBearId));

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

    @Test
    @DisplayName("Neither creature fights when the first target leaves before resolution")
    void neitherFightsWhenFirstTargetRemoved() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new PitFight()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, List.of(bear.getId(), elves.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(bear);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(elves.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Pit Fight");
    }

    @Test
    @DisplayName("Neither creature fights when the first target is no longer controlled by the caster")
    void neitherFightsWhenFirstTargetChangesController() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new PitFight()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, List.of(bear.getId(), elves.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerBattlefields.get(player2.getId()).add(bear);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(elves.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Pit Fight");
    }

    @Test
    @DisplayName("Tapped creatures can fight using current power and green hybrid mana")
    void tappedCreaturesDealNonlethalDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        first.setTapped(true);
        second.setTapped(true);
        first.setPowerModifier(-1);
        second.setPowerModifier(-2);
        harness.setHand(player1, List.of(new PitFight()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }
}
