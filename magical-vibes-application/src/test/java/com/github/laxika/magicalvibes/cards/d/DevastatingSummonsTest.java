package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulsAttendant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevastatingSummons.class, Forest.class, GrizzlyBears.class, SoulsAttendant.class})
class DevastatingSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing two lands creates two 2/2 Elementals")
    void sacrificingLandsCreatesElementalsWithThatPowerAndToughness() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new DevastatingSummons()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifices(player1, 0, null, List.of(firstLand.getId(), secondLand.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(elemental -> {
                    assertThat(elemental.getEffectivePower()).isEqualTo(2);
                    assertThat(elemental.getEffectiveToughness()).isEqualTo(2);
                });
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot sacrifice a nonland to pay the additional cost")
    void cannotSacrificeNonland() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new DevastatingSummons()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, null, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing zero lands is legal and the 0/0 tokens die")
    void canSacrificeZeroLands() {
        harness.setHand(player1, List.of(new DevastatingSummons()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifices(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Devastating Summons");
    }

    @Test
    @DisplayName("A tapped land is sacrificed during casting before any tokens are created")
    void tappedLandIsPaidBeforeResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();
        harness.setHand(player1, List.of(new DevastatingSummons()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifices(player1, 0, null, List.of(land.getId()));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allSatisfy(elemental -> {
                    assertThat(elemental.getCard().isToken()).isTrue();
                    assertThat(elemental.getEffectivePower()).isEqualTo(1);
                    assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot count the same land twice when paying the cost")
    void cannotSacrificeTheSameLandTwice() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new DevastatingSummons()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(
                player1, 0, null, List.of(land.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Devastating Summons");
    }

    @Test
    @DisplayName("An invalid selection cannot sacrifice either player's lands")
    void cannotSacrificeOpponentsLand() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DevastatingSummons()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(
                player1, 0, null, List.of(ownLand.getId(), opposingLand.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertInHand(player1, "Devastating Summons");
    }

    @Test
    @DisplayName("Both zero-toughness tokens enter and trigger abilities before dying")
    void zeroLandTokensStillTriggerCreatureEntryAbilities() {
        harness.addToBattlefield(player1, new SoulsAttendant());
        harness.setHand(player1, List.of(new DevastatingSummons()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifices(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.assertOnBattlefield(player1, "Soul's Attendant");
        assertThat(gd.stack).hasSize(2);
    }
}
