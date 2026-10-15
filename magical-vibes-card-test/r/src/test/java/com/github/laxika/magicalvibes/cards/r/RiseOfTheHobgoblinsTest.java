package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiseOfTheHobgoblins.class, HillGiant.class, EliteVanguard.class, FugitiveWizard.class,
        Opalescence.class})
class RiseOfTheHobgoblinsTest extends BaseCardTest {

    private void setupPlayer1Active() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private long goblinTokenCount() {
        return countPermanents(player1, "Goblin Soldier");
    }

    @Test
    @DisplayName("Resolving the enter trigger with mana prompts for X value choice")
    void enterTriggerPromptsForX() {
        setupPlayer1Active();
        harness.setHand(player1, java.util.List.of(new RiseOfTheHobgoblins()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        PendingInteraction.XValueChoice ctx =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(ctx).isNotNull();
        assertThat(ctx.playerId()).isEqualTo(player1.getId());
        // {R/W}{R/W} cast leaves 2 of the 4 red mana for X
        assertThat(ctx.maxValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing X creates X 1/1 Goblin Soldier tokens and pays that much mana")
    void choosingXCreatesTokens() {
        setupPlayer1Active();
        harness.setHand(player1, java.util.List.of(new RiseOfTheHobgoblins()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        harness.handleXValueChosen(player1, 2);

        assertThat(goblinTokenCount()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        Permanent token = findPermanent(player1, "Goblin Soldier");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing X=0 creates no tokens and pays no mana")
    void choosingZeroCreatesNothing() {
        setupPlayer1Active();
        harness.setHand(player1, java.util.List.of(new RiseOfTheHobgoblins()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        harness.handleXValueChosen(player1, 0);

        assertThat(goblinTokenCount()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("With no leftover mana the enter trigger makes no tokens and gives no prompt")
    void noManaNoTokens() {
        setupPlayer1Active();
        harness.setHand(player1, java.util.List.of(new RiseOfTheHobgoblins()));
        harness.addMana(player1, ManaColor.RED, 2); // exactly the cast cost

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(goblinTokenCount()).isZero();
    }

    @Test
    @DisplayName("Ability grants first strike to red and white creatures you control, not others")
    void abilityGrantsFirstStrikeToRedAndWhite() {
        harness.addToBattlefield(player1, new RiseOfTheHobgoblins());
        Permanent red = addCreatureReady(player1, new HillGiant());       // red
        Permanent white = addCreatureReady(player1, new EliteVanguard()); // white
        Permanent blue = addCreatureReady(player1, new FugitiveWizard()); // blue
        setupPlayer1Active();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, red, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, white, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, blue, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Ability does not affect opponent's red creatures")
    void abilityDoesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new RiseOfTheHobgoblins());
        Permanent opponentRed = addCreatureReady(player2, new HillGiant());
        setupPlayer1Active();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentRed, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        harness.addToBattlefield(player1, new RiseOfTheHobgoblins());
        Permanent red = addCreatureReady(player1, new HillGiant());
        setupPlayer1Active();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, red, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, red, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The ability grants first strike to Rise of the Hobgoblins when it is a creature")
    void animatedEnchantmentGainsFirstStrike() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent rise = harness.addToBattlefieldAndReturn(player1, new RiseOfTheHobgoblins());
        setupPlayer1Active();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, rise)).isTrue();
        assertThat(gqs.hasKeyword(gd, rise, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ability affects creatures present at resolution but not creatures entering afterward")
    void firstStrikeAppliesOnlyToCreaturesPresentAtResolution() {
        harness.addToBattlefield(player1, new RiseOfTheHobgoblins());
        setupPlayer1Active();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = addCreatureReady(player1, new HillGiant());

        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new HillGiant());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Created Goblin Soldier tokens gain first strike when the ability is activated")
    void createdTokensGainFirstStrike() {
        setupPlayer1Active();
        harness.setHand(player1, java.util.List.of(new RiseOfTheHobgoblins()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.handleXValueChosen(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Soldier")).hasSize(2).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isTrue());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
