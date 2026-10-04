package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DeconstructionHammer;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianOfTheGreatDoor.class, GrizzlyBears.class, Spellbook.class, Forest.class,
        DeconstructionHammer.class, GrowingRitesOfItlimoc.class})
class GuardianOfTheGreatDoorTest extends BaseCardTest {

    @Test
    @DisplayName("Taps four artifacts, creatures, and/or lands as an additional cost")
    void tapsFourEligiblePermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        harness.castCreatureTappingPermanents(player1, 0,
                List.of(artifact.getId(), firstCreature.getId(), secondCreature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Guardian of the Great Door");
    }

    @Test
    @DisplayName("Requires exactly four eligible permanents")
    void rejectsTheWrongNumberOfPermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(artifact.getId(), firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
        assertThat(firstCreature.isTapped()).isFalse();
        assertThat(secondCreature.isTapped()).isFalse();
        harness.assertInHand(player1, "Guardian of the Great Door");
    }

    @Test
    void paysMixedCostBeforeResolutionIncludingSummoningSickCreatures() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DeconstructionHammer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GuardianOfTheGreatDoor());
        creature.setSummoningSick(true);
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        harness.castCreatureTappingPermanents(player1, 0,
                List.of(artifact.getId(), creature.getId(), firstLand.getId(), secondLand.getId()));

        assertThat(List.of(artifact, creature, firstLand, secondLand)).allMatch(Permanent::isTapped);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player1, "Guardian of the Great Door");
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Guardian of the Great Door")).isEqualTo(2);
    }

    @Test
    void canPayWithFourLands() {
        List<Permanent> lands = List.of(
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()));
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        harness.castCreatureTappingPermanents(player1, 0, lands.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(lands).allMatch(Permanent::isTapped);
        harness.assertOnBattlefield(player1, "Guardian of the Great Door");
    }

    @Test
    void rejectsDuplicatePermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(List.of(first, second, third)).noneMatch(Permanent::isTapped);
        harness.assertInHand(player1, "Guardian of the Great Door");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsAlreadyTappedPermanentsWithoutTappingOthers() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new Forest());
        fourth.tap();
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(List.of(first, second, third)).noneMatch(Permanent::isTapped);
        assertThat(fourth.isTapped()).isTrue();
        harness.assertInHand(player1, "Guardian of the Great Door");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsOpponentsPermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), opposing.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(List.of(first, second, third, opposing)).noneMatch(Permanent::isTapped);
        harness.assertInHand(player1, "Guardian of the Great Door");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsEnchantmentThatIsNotAnArtifactCreatureOrLand() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GrowingRitesOfItlimoc());
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(List.of(first, second, third, enchantment)).noneMatch(Permanent::isTapped);
        harness.assertInHand(player1, "Guardian of the Great Door");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotOmitAdditionalCost() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
        harness.assertInHand(player1, "Guardian of the Great Door");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsMoreThanFourPermanents() {
        List<Permanent> lands = List.of(
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()));
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                lands.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lands).noneMatch(Permanent::isTapped);
        harness.assertInHand(player1, "Guardian of the Great Door");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappingPermanentsDoesNotReplaceManaCost() {
        List<Permanent> lands = List.of(
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()));
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                lands.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lands).noneMatch(Permanent::isTapped);
        harness.assertInHand(player1, "Guardian of the Great Door");
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
