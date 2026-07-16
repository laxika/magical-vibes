package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RithTheAwakenerTest extends BaseCardTest {

    private long saprolingCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .count();
    }

    private Permanent addAttackingRith() {
        Permanent rith = addCreatureReady(player1, new RithTheAwakener());
        rith.setAttacking(true);
        return rith;
    }

    private void resolveCombatToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
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
        addCreatureReady(player2, new GrizzlyBears());     // green creature

        resolveCombatToMayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        // Rith and the Grizzly Bears are green; the Forest (land) is excluded.
        assertThat(saprolingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing a color no permanent has creates no tokens")
    void noMatchesCreatesNoTokens() {
        addAttackingRith();

        resolveCombatToMayPrompt();

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

        resolveCombatToMayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(saprolingCount()).isZero();
    }
}
