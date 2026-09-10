package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneWyvern.class, FlowstoneGiant.class})
class FlowstoneWyvernTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +2/-2 to Flowstone Wyvern")
    void resolvingAbilityBoosts() {
        addCreatureReady(player1, new FlowstoneWyvern());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent wyvern = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wyvern.getEffectivePower()).isEqualTo(5);
        assertThat(wyvern.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating twice kills Flowstone Wyvern via state-based actions")
    void toughnessDropsToZeroAndItDies() {
        addCreatureReady(player1, new FlowstoneWyvern());
        harness.addMana(player1, ManaColor.RED, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new FlowstoneWyvern());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wyvern = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wyvern.getEffectivePower()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wyvern.getEffectivePower()).isEqualTo(3);
        assertThat(wyvern.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new FlowstoneWyvern());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Flying prevents Flowstone Wyvern from being blocked by a nonflying creature")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new FlowstoneWyvern());
        addCreatureReady(player2, new FlowstoneGiant());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
