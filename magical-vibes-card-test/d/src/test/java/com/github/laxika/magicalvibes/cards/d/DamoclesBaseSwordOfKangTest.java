package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CanyonSlough;
import com.github.laxika.magicalvibes.cards.i.IronMongerSadisticTycoon;
import com.github.laxika.magicalvibes.cards.t.TheValeyard;
import com.github.laxika.magicalvibes.cards.z.ZuriWarriorOfWakanda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DamoclesBaseSwordOfKang.class, ZuriWarriorOfWakanda.class,
        IronMongerSadisticTycoon.class, CanyonSlough.class, TheValeyard.class})
class DamoclesBaseSwordOfKangTest extends BaseCardTest {

    private static final String SACRIFICE = "Sacrifice a nontoken creature";
    private static final String LIFE_AND_DRAW = "Lose 2 life and draw two cards";

    @Test
    @DisplayName("The damaged player chooses between sacrificing and losing life to draw")
    void damagedPlayerChoosesVillainousChoice() {
        Permanent damocles = addDamocles();
        addCreatureReady(player2, new ZuriWarriorOfWakanda());
        damocles.setAttacking(true);

        resolveCombatAndTrigger();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(SACRIFICE, LIFE_AND_DRAW);
    }

    @Test
    @DisplayName("Sacrificing a nontoken creature prevents the life loss and draw")
    void sacrificeBranch() {
        Permanent damocles = addDamocles();
        addCreatureReady(player2, new ZuriWarriorOfWakanda());
        damocles.setAttacking(true);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, SACRIFICE);

        harness.assertNotOnBattlefield(player2, "Zuri, Warrior of Wakanda");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertInGraveyard(player2, "Zuri, Warrior of Wakanda");
    }

    @Test
    @DisplayName("The life branch makes the damaged player lose life and draws two cards")
    void lifeAndDrawBranch() {
        Permanent damocles = addDamocles();
        damocles.setAttacking(true);
        List<Card> library = List.of(new CanyonSlough(), new CanyonSlough());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, LIFE_AND_DRAW);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 7);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyElementsOf(library.stream().map(Card::getId).toList());
    }

    @Test
    void crewRequiresAtLeastThreePower() {
        Permanent damocles = addCreatureReady(player1, new DamoclesBaseSwordOfKang());
        Permanent crew = addCreatureReady(player1, new ZuriWarriorOfWakanda());

        assertThat(gqs.isCreature(gd, damocles)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(crew.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, damocles)).isFalse();
    }

    @Test
    void crewTapsCreaturesAndAnimatesUntilEndOfTurn() {
        Permanent damocles = addDamocles();

        assertThat(gqs.isCreature(gd, damocles)).isTrue();
        assertThat(findPermanent(player1, "Zuri, Warrior of Wakanda").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Iron Monger, Sadistic Tycoon").isTapped()).isTrue();
        assertThat(damocles.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, damocles)).isFalse();
    }

    @Test
    void damagedPlayerChoosesWhichNontokenCreatureToSacrifice() {
        Permanent damocles = addDamocles();
        Permanent chosen = addCreatureReady(player2, new ZuriWarriorOfWakanda());
        Permanent other = addCreatureReady(player2, new IronMongerSadisticTycoon());
        harness.addToBattlefield(player2, new CanyonSlough());
        damocles.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, SACRIFICE);
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        harness.assertInGraveyard(player2, "Zuri, Warrior of Wakanda");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(other);
        harness.assertOnBattlefield(player2, "Canyon Slough");
        harness.assertLife(player2, lifeBefore - 5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void impossibleSacrificeCanBeChosenWhenOnlyTokenCreaturesExist() {
        Permanent damocles = addDamocles();
        IronMongerSadisticTycoon tokenCard = new IronMongerSadisticTycoon();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player2, tokenCard);
        damocles.setAttacking(true);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, SACRIFICE);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
        harness.assertLife(player2, lifeBefore - 5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void lifeAndDrawCanBeChosenEvenWhenSacrificeIsPossible() {
        Permanent damocles = addDamocles();
        Permanent creature = addCreatureReady(player2, new ZuriWarriorOfWakanda());
        List<Card> library = List.of(new CanyonSlough(), new CanyonSlough());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        damocles.setAttacking(true);

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, LIFE_AND_DRAW);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertLife(player2, lifeBefore - 7);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }

    @Test
    void valeyardMakesDamagedOpponentFaceTheChoiceTwice() {
        Permanent damocles = addDamocles();
        harness.addToBattlefield(player1, new TheValeyard());
        List<Card> library = List.of(new CanyonSlough(), new CanyonSlough());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        damocles.setAttacking(true);

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, SACRIFICE);

        PendingInteraction.ColorChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.options()).containsExactly(SACRIFICE, LIFE_AND_DRAW);
        harness.handleListChoice(player2, LIFE_AND_DRAW);

        harness.assertLife(player2, lifeBefore - 7);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addDamocles() {
        Permanent damocles = addCreatureReady(player1, new DamoclesBaseSwordOfKang());
        addCreatureReady(player1, new ZuriWarriorOfWakanda());
        addCreatureReady(player1, new IronMongerSadisticTycoon());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return damocles;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
