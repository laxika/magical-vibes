package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssertPerfection.class, GrizzlyBears.class, LlanowarElves.class, AirElemental.class})
class AssertPerfectionTest extends BaseCardTest {


    @Test
    @DisplayName("Boost only â€” single target creature you control gets +1/+0")
    void singleTargetBoostOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId));

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost and bite â€” creature gets +1/+0 and deals power damage to opponent creature")
    void boostAndBiteKillsSmallCreature() {
        // Grizzly Bears is 2/2. After +1/+0 it becomes 3/2, dealing 3 damage to Llanowar Elves (1/1)
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId, elvesId));

        // Bear should be boosted
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(1);

        // Llanowar Elves should be destroyed (3 damage >= 1 toughness)
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Bite deals damage but does not kill a tougher creature")
    void biteDamagesButDoesNotKill() {
        // Grizzly Bears is 2/2. After +1/+0 it becomes 3/2, dealing 3 damage to Air Elemental (4/4)
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId, elementalId));

        // Bear should be boosted
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(1);

        // Air Elemental should survive (3 damage < 4 toughness)
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target own creature as second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1);
        harness.addToBattlefield(player1, bear2);
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot target opponent creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new LlanowarElves()); // needed so the spell is castable
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Spell fizzles when all targets removed before resolution")
    void fizzlesWhenAllTargetsRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(bearId, elvesId));

        // Remove both targets before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Boost still applies when second target removed before resolution")
    void boostAppliesWhenSecondTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(bearId, elvesId));

        // Remove only the second target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Boost should still apply to the first target
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Bite does nothing when first target removed before resolution")
    void biteDoesNothingWhenFirstTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(bearId, elvesId));

        // Remove only the first target (biter) before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Spell partially resolves â€” Llanowar Elves should still be alive (no biter)
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }
    @Test
    @DisplayName("Boost expires at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent bear = findPermanent(player1, "Grizzly Bears");

        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId()));
        assertThat(bear.getPowerModifier()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Bite uses boosted power and deals no damage back to its source")
    void biteUsesBoostedPowerWithoutReturnDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        Permanent elemental = findPermanent(player2, "Air Elemental");

        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId(), elemental.getId()));

        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("First target becoming opponent-controlled prevents both boost and bite")
    void firstTargetChangingControllerIsIllegal() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        Permanent elemental = findPermanent(player2, "Air Elemental");
        harness.castSorcery(player1, 0, List.of(bear.getId(), elemental.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerBattlefields.get(player2.getId()).add(bear);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(elemental.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Assert Perfection");
    }

    @Test
    @DisplayName("Second target becoming friendly prevents damage but preserves the boost")
    void secondTargetChangingControllerIsIllegal() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new AssertPerfection()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        Permanent elemental = findPermanent(player2, "Air Elemental");
        harness.castSorcery(player1, 0, List.of(bear.getId(), elemental.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(elemental);
        gd.playerBattlefields.get(player1.getId()).add(elemental);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(elemental.getMarkedDamage()).isZero();
    }
}
