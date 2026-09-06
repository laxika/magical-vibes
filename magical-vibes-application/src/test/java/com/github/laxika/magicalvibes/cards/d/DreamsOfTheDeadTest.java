package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.BlinkingSpirit;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.m.MoorFiend;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalduvianBears.class, BlinkingSpirit.class, DreamsOfTheDead.class, Incinerate.class,
        MoorFiend.class, SwordsToPlowshares.class})
class DreamsOfTheDeadTest extends BaseCardTest {

    private Permanent reanimate(Card creature) {
        Permanent dreams = harness.addToBattlefieldAndReturn(player1, new DreamsOfTheDead());
        harness.setGraveyard(player1, List.of(creature));
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

    @Test
    @DisplayName("Returns target white creature from graveyard with CU {2} and exile-if-leaves")
    void returnsWhiteCreatureWithRiders() {
        Permanent spirit = reanimate(new BlinkingSpirit());

        assertThat(spirit.hasCumulativeUpkeep()).isTrue();
        assertThat(spirit.isExileIfLeavesBattlefield()).isTrue();
        harness.assertNotInGraveyard(player1, "Blinking Spirit");
    }

    @Test
    @DisplayName("Returns target black creature from graveyard")
    void returnsBlackCreature() {
        Permanent fiend = reanimate(new MoorFiend());

        assertThat(fiend.hasCumulativeUpkeep()).isTrue();
        assertThat(fiend.isExileIfLeavesBattlefield()).isTrue();
    }

    @Test
    @DisplayName("Requires {1}{U} to activate")
    void requiresBlueAndGenericMana() {
        Card spirit = new BlinkingSpirit();
        Permanent dreams = harness.addToBattlefieldAndReturn(player1, new DreamsOfTheDead());
        harness.setGraveyard(player1, List.of(spirit));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(dreams), 0, List.of(spirit.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a green creature in the graveyard")
    void cannotTargetGreenCreature() {
        Card bears = new BalduvianBears();
        Permanent dreams = harness.addToBattlefieldAndReturn(player1, new DreamsOfTheDead());
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(dreams), 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature white card in the graveyard")
    void cannotTargetNoncreatureWhiteCard() {
        Card swords = new SwordsToPlowshares();
        Permanent dreams = harness.addToBattlefieldAndReturn(player1, new DreamsOfTheDead());
        harness.setGraveyard(player1, List.of(swords));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(dreams), 0, List.of(swords.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new MoorFiend();
        Permanent dreams = harness.addToBattlefieldAndReturn(player1, new DreamsOfTheDead());
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(dreams), 0, List.of(creature.getId())))
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
        harness.assertNotInGraveyard(player1, "Blinking Spirit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blinking Spirit"));
    }

    @Test
    @DisplayName("Destroying the reanimated creature exiles it instead")
    void destroyExilesInsteadOfGraveyard() {
        Permanent spirit = reanimate(new BlinkingSpirit());

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spirit);
        harness.assertNotInGraveyard(player1, "Blinking Spirit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blinking Spirit"));
    }

    @Test
    @DisplayName("Returning the reanimated creature to hand exiles it instead")
    void returningReanimatedCreatureToHandExilesIt() {
        Permanent spirit = reanimate(new BlinkingSpirit());

        harness.activateAbility(player1, battlefieldIndex(spirit), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spirit);
        harness.assertNotInHand(player1, "Blinking Spirit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blinking Spirit"));
    }
}
