package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RithTheAwakener.class, Forest.class, NomadicElf.class, DryadArbor.class})
class RithTheAwakenerTest extends BaseCardTest {

    private long saprolingCount() {
        return countPermanents(player1, "Saproling");
    }

    private Permanent addAttackingRith() {
        Permanent rith = addCreatureReady(player1, new RithTheAwakener());
        rith.setAttacking(true);
        return rith;
    }

    // Mana must be added after combat is resolved: step transitions empty the mana pool,
    // and the {2}{G} payment happens in the combat damage step at the may-ability prompt.
    private void addPaymentMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Paying {2}{G} and choosing a color creates a Saproling per permanent of that color")
    void createsTokenPerPermanentOfChosenColor() {
        addAttackingRith();                       // Rith is green (among R/G/W)
        harness.addToBattlefield(player1, new Forest());   // land — excluded
        addCreatureReady(player2, new NomadicElf());       // green creature

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        // Rith and the Nomadic Elf are green; the Forest is colorless.
        assertThat(saprolingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing a color no permanent has creates no tokens")
    void noMatchesCreatesNoTokens() {
        addAttackingRith();

        resolveCombat();
        harness.passBothPriorities();

        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(saprolingCount()).isZero();
    }

    @Test
    @DisplayName("Declining the payment creates no tokens and asks for no color")
    void decliningCreatesNoTokens() {
        addAttackingRith();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(saprolingCount()).isZero();
    }

    @Test
    @DisplayName("Cannot pay the cost skips the optional ability")
    void cannotPaySkipsAbility() {
        addAttackingRith();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(saprolingCount()).isZero();
    }

    @Test
    @DisplayName("Counts a colored land creature as a permanent of the chosen color")
    void countsColoredLandCreature() {
        addAttackingRith();
        addCreatureReady(player2, new DryadArbor());

        resolveCombat();
        harness.passBothPriorities();

        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "GREEN");

        assertThat(saprolingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when Rith deals combat damage only to a creature")
    void noTriggerWhenBlocked() {
        addAttackingRith();
        Permanent blocker = addCreatureReady(player2, new RithTheAwakener());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(saprolingCount()).isZero();
    }

    @Test
    @DisplayName("Three colorless mana cannot pay the green component of the cost")
    void cannotPayWithoutGreenMana() {
        addAttackingRith();

        resolveCombat();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(saprolingCount()).isZero();
    }

    @Test
    @DisplayName("Choosing red counts multicolored Rith once")
    void countsMulticoloredPermanentForRed() {
        addAttackingRith();
        addCreatureReady(player2, new NomadicElf());

        resolveCombat();
        harness.passBothPriorities();

        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "RED");

        assertThat(saprolingCount()).isEqualTo(1);
        assertThat(countPermanents(player2, "Saproling")).isZero();
    }

    @Test
    @DisplayName("Counts permanents at resolution even if Rith has left the battlefield")
    void countsCurrentPermanentsAfterSourceLeaves() {
        Permanent rith = addAttackingRith();

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(rith);
        harness.setGraveyard(player1, List.of(rith.getCard()));
        addCreatureReady(player2, new NomadicElf());
        addCreatureReady(player2, new NomadicElf());
        harness.passBothPriorities();

        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "GREEN");

        assertThat(saprolingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("A later trigger counts existing Saprolings without counting its newly created tokens")
    void countsExistingTokensOnLaterTrigger() {
        Permanent rith = addAttackingRith();

        resolveCombat();
        harness.passBothPriorities();
        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "GREEN");
        assertThat(saprolingCount()).isEqualTo(1);

        rith.setTapped(false);
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "GREEN");

        assertThat(saprolingCount()).isEqualTo(3);
    }
}
