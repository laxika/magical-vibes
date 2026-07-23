package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlinkingSpirit;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VampireBats;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreamsOfTheDeadTest extends BaseCardTest {

    private Permanent reanimate(Card creature) {
        Permanent dreams = harness.addToBattlefieldAndReturn(player1, new DreamsOfTheDead());
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature)));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(dreams), 0, List.of(creature.getId()));
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns target white creature from graveyard with CU {2} and exile-if-leaves")
    void returnsWhiteCreatureWithRiders() {
        Permanent spirit = reanimate(new BlinkingSpirit());

        assertThat(spirit.hasCumulativeUpkeep()).isTrue();
        assertThat(spirit.isExileIfLeavesBattlefield()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Blinking Spirit"));
    }

    @Test
    @DisplayName("Returns target black creature from graveyard")
    void returnsBlackCreature() {
        Permanent bats = reanimate(new VampireBats());

        assertThat(bats.hasCumulativeUpkeep()).isTrue();
        assertThat(bats.isExileIfLeavesBattlefield()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a green creature in the graveyard")
    void cannotTargetGreenCreature() {
        Card bears = new GrizzlyBears();
        Permanent dreams = harness.addToBattlefieldAndReturn(player1, new DreamsOfTheDead());
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(dreams), 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted cumulative upkeep can be paid")
    void payingGrantedCumulativeUpkeepKeepsCreature() {
        Permanent spirit = reanimate(new BlinkingSpirit());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(spirit.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spirit);
    }

    @Test
    @DisplayName("Declining granted cumulative upkeep exiles the creature")
    void decliningCumulativeUpkeepExiles() {
        Permanent spirit = reanimate(new BlinkingSpirit());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spirit);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Blinking Spirit"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blinking Spirit"));
    }

    @Test
    @DisplayName("Destroying the reanimated creature exiles it instead")
    void destroyExilesInsteadOfGraveyard() {
        Permanent spirit = reanimate(new BlinkingSpirit());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        // Blinking Spirit is 2/2 — Shock deals 2.
        harness.castInstant(player2, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spirit);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Blinking Spirit"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blinking Spirit"));
    }
}
