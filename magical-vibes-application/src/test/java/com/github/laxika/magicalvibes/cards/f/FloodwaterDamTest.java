package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.s.SchoolOfTheUnseen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloodwaterDam.class, SchoolOfTheUnseen.class, AesthirGlider.class})
class FloodwaterDamTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 taps two target lands for a total cost of five mana")
    void tapsXTargetLands() {
        harness.addToBattlefield(player1, new FloodwaterDam());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SchoolOfTheUnseen());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SchoolOfTheUnseen());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each {X} symbol is paid separately, so X=2 cannot be activated with four mana")
    void doubleXIsChargedTwice() {
        harness.addToBattlefield(player1, new FloodwaterDam());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SchoolOfTheUnseen());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SchoolOfTheUnseen());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("More targets than the paid X are rejected")
    void rejectsMoreTargetsThanX() {
        harness.addToBattlefield(player1, new FloodwaterDam());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SchoolOfTheUnseen());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SchoolOfTheUnseen());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature is an illegal target")
    void rejectsNonLandTarget() {
        harness.addToBattlefield(player1, new FloodwaterDam());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AesthirGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=2 requires two distinct legal land targets")
    void requiresExactlyXTargets() {
        harness.addToBattlefield(player1, new FloodwaterDam());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SchoolOfTheUnseen());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 allows the ability to be activated without targets")
    void allowsZeroTargetsWhenXIsZero() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new FloodwaterDam());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SchoolOfTheUnseen());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The same land cannot be selected more than once")
    void rejectsDuplicateTarget() {
        harness.addToBattlefield(player1, new FloodwaterDam());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SchoolOfTheUnseen());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(land.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
