package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningWolf.class})
class LightningWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Lightning Wolf grants it first strike until end of turn")
    void activationGrantsFirstStrikeUntilEndOfTurn() {
        Permanent wolf = addCreatureReady(player1, new LightningWolf());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Lightning Wolf's ability can only be activated at sorcery speed")
    void activationRequiresSorcerySpeed() {
        addCreatureReady(player1, new LightningWolf());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Lightning Wolf cannot activate during an opponent's main phase")
    void activationRequiresOwnTurn() {
        addCreatureReady(player1, new LightningWolf());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lightning Wolf cannot activate again while its ability is on the stack")
    void activationRequiresEmptyStack() {
        Permanent wolf = addCreatureReady(player1, new LightningWolf());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.stack).hasSize(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A tapped summoning-sick Lightning Wolf grants first strike only to itself")
    void activationDoesNotRequireUntappedOrReadyCreature() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new LightningWolf());
        wolf.setSummoningSick(true);
        wolf.tap();
        Permanent otherWolf = addCreatureReady(player1, new LightningWolf());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherWolf, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(wolf.isTapped()).isTrue();
    }
}
