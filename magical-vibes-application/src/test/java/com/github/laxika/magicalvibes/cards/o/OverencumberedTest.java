package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Overencumbered.class, SuntailHawk.class})
class OverencumberedTest extends BaseCardTest {

    @Test
    @DisplayName("When Overencumbered enters, the enchanted opponent creates a Clue, Food, and Junk")
    void createsArtifactTokensForEnchantedOpponent() {
        attachOverencumbered();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(p -> p.getCard().getName())
                .contains("Clue", "Food", "Junk");
        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(
                p -> p.getCard().getSubtypes().contains(CardSubtype.CLUE)).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(
                p -> p.getCard().getSubtypes().contains(CardSubtype.FOOD)).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(
                p -> p.getCard().getSubtypes().contains(CardSubtype.JUNK)).hasSize(1);
    }

    @Test
    @DisplayName("Declining the artifact payment prevents creatures from attacking this combat")
    void decliningPaymentPreventsAttacks() {
        attachOverencumbered();
        addCreatureReady(player2, new SuntailHawk());

        resolveCombatTrigger(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard() instanceof SuntailHawk)
                .allMatch(p -> !p.isAttacking());
    }

    @Test
    @DisplayName("Paying for all controlled artifacts allows attacks")
    void payingForArtifactsAllowsAttacks() {
        attachOverencumbered();
        addCreatureReady(player2, new SuntailHawk());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        resolveCombatTrigger(player2);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        declareAttackers(player2);
    }

    private void attachOverencumbered() {
        harness.setHand(player1, List.of(new Overencumbered()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveCombatTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private void declareAttackers(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        List<Permanent> battlefield = gd.playerBattlefields.get(attacker.getId());
        int attackerIndex = IntStream.range(0, battlefield.size())
                .filter(i -> battlefield.get(i).getCard() instanceof SuntailHawk)
                .findFirst()
                .orElseThrow();
        gs.declareAttackers(gd, attacker, List.of(attackerIndex));
    }
}
