package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NehebDreadhordeChampion.class, GrizzlyBears.class, JayaVeneratedFiremage.class})
class NehebDreadhordeChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage trigger draws and adds red mana for cards discarded")
    void drawsAndAddsManaForDiscardedCards() {
        Permanent neheb = addCreatureReady(player1, new NehebDreadhordeChampion());
        neheb.setAttacking(true);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getPersistentMana(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Choosing zero cards does not draw or add mana")
    void choosingZeroDoesNothing() {
        Permanent neheb = addCreatureReady(player1, new NehebDreadhordeChampion());
        neheb.setAttacking(true);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("An empty hand does not draw cards or add mana based on combat damage")
    void emptyHandDoesNothing() {
        Permanent neheb = addCreatureReady(player1, new NehebDreadhordeChampion());
        neheb.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLibraries.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("All cards can be discarded and only the generated red mana persists until turn end")
    void discardsEntireHandAndManaExpires() {
        Permanent neheb = addCreatureReady(player1, new NehebDreadhordeChampion());
        neheb.setAttacking(true);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombat();
        resolveAllTriggers();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLibraries.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getPersistentMana(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Combat damage to a planeswalker triggers even when that planeswalker dies")
    void planeswalkerDamageTriggers() {
        Permanent jaya = harness.addToBattlefieldAndReturn(player2, new JayaVeneratedFiremage());
        jaya.setCounterCount(CounterType.LOYALTY, 5);
        Permanent neheb = addCreatureReady(player1, new NehebDreadhordeChampion());
        neheb.setAttacking(true);
        neheb.setAttackTarget(jaya.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(jaya);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage only to a creature does not trigger the discard ability")
    void creatureDamageDoesNotTrigger() {
        Permanent neheb = addCreatureReady(player1, new NehebDreadhordeChampion());
        neheb.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new NehebDreadhordeChampion());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player1, List.of(new NehebDreadhordeChampion()));
        harness.setLibrary(player1, List.of(new NehebDreadhordeChampion()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The attacking Neheb's controller discards, draws, and receives the mana")
    void secondPlayerReceivesBenefits() {
        Permanent neheb = addCreatureReady(player2, new NehebDreadhordeChampion());
        neheb.setAttacking(true);
        harness.setHand(player2, List.of(new NehebDreadhordeChampion()));
        harness.setLibrary(player2, List.of(new NehebDreadhordeChampion()));
        harness.setHand(player1, List.of());

        resolveCombat(player2);
        resolveAllTriggers();
        harness.handleXValueChosen(player2, 1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
