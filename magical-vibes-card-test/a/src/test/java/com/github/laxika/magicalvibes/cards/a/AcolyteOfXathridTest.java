package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcolyteOfXathrid.class})
class AcolyteOfXathridTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability causes target player to lose 1 life")
    void activateAbilityTargetLosesLife() {
        addCreatureReady(player1, new AcolyteOfXathrid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot pay the black mana requirement with only colorless mana")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new AcolyteOfXathrid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new AcolyteOfXathrid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature with the player-targeted ability")
    void cannotTargetCreature() {
        Permanent acolyte = addCreatureReady(player1, new AcolyteOfXathrid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, acolyte.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability uses the stack and resolves after its source leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent acolyte = addCreatureReady(player1, new AcolyteOfXathrid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(acolyte.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        gd.playerBattlefields.get(player1.getId()).remove(acolyte);
        gd.playerGraveyards.get(player1.getId()).add(acolyte.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target yourself with the ability")
    void canTargetSelf() {
        addCreatureReady(player1, new AcolyteOfXathrid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new AcolyteOfXathrid());
        harness.addMana(player1, ManaColor.BLACK, 1); // need {1}{B}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new AcolyteOfXathrid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        // First activation taps it
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
