package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelekineticSliver.class, BonescytheSliver.class, Forest.class, GrizzlyBears.class})
class TelekineticSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Telekinetic Sliver grants itself the ability to tap any permanent")
    void grantsAbilityToItself() {
        Permanent telekineticSliver = addCreatureReady(player1, new TelekineticSliver());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(telekineticSliver.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Sliver gains the ability and can tap a land")
    void grantsAbilityToAnotherSliver() {
        addCreatureReady(player1, new TelekineticSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(otherSliver.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Sliver also gains the ability")
    void grantsAbilityToOpposingSliver() {
        addCreatureReady(player1, new TelekineticSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(opposingSliver.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSliver() {
        addCreatureReady(player1, new TelekineticSliver());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability cannot target a player")
    void rejectsPlayerTarget() {
        addCreatureReady(player1, new TelekineticSliver());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
