package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindreaderSphinx.class, SuntailHawk.class, CoralMerfolk.class})
class WindreaderSphinxTest extends BaseCardTest {

    private void addSphinx() {
        harness.addToBattlefield(player1, new WindreaderSphinx());
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(owner, card);
        perm.setSummoningSick(false);
        return perm;
    }

    private void declareAttack(Player attacker, Integer... indices) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, attacker, List.of(indices));
    }

    @Test
    @DisplayName("An opponent's attacking flier offers the Sphinx controller a may-draw")
    void opponentFlierTriggers() {
        addSphinx();
        addAttacker(player2, new SuntailHawk());

        declareAttack(player2, 0);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the trigger draws no card")
    void decliningDrawsNoCard() {
        addSphinx();
        addAttacker(player2, new SuntailHawk());

        declareAttack(player2, 0);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("An attacking creature without flying doesn't trigger the ability")
    void nonFlierDoesNotTrigger() {
        addSphinx();
        addAttacker(player2, new CoralMerfolk());

        declareAttack(player2, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Sphinx's own attack triggers its ability")
    void ownFlierTriggers() {
        addSphinx();

        // The Sphinx itself is the only permanent, at index 0 on player1's battlefield.
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);
        declareAttack(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Each attacking flier creates a separate optional draw")
    void multipleFliersOfferIndependentChoices() {
        addSphinx();
        addAttacker(player2, new SuntailHawk());
        addAttacker(player2, new SuntailHawk());
        addAttacker(player2, new CoralMerfolk());
        harness.setLibrary(player1, List.of(new SuntailHawk(), new SuntailHawk()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();

        declareAttack(player2, 0, 1, 2);

        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The optional draw survives the Sphinx and attacker leaving the battlefield")
    void triggerSurvivesSourceAndAttackerLeaving() {
        addSphinx();
        addAttacker(player2, new SuntailHawk());
        harness.setLibrary(player1, List.of(new SuntailHawk()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttack(player2, 0);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }
}
