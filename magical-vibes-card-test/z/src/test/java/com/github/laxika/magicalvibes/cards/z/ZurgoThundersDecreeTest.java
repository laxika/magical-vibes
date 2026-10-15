package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.cards.s.SaheelisArtistry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZurgoThundersDecree.class, ObsidianBattleAxe.class, SaheelisArtistry.class})
class ZurgoThundersDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates two tapped and attacking Warrior tokens")
    void attackingCreatesTwoMobilizedTokens() {
        addCreatureReady(player1, new ZurgoThundersDecree());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = warriorTokens(player1);
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
        });
    }

    @Test
    @DisplayName("Warrior tokens survive their controller's next end step")
    void warriorTokensSurviveYourEndStep() {
        addCreatureReady(player1, new ZurgoThundersDecree());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        advanceToEndStep(player1);

        assertThat(warriorTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Warrior tokens are sacrificed at an opponent's end step")
    void warriorTokensAreSacrificedAtOpponentsEndStep() {
        addCreatureReady(player1, new ZurgoThundersDecree());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        advanceToEndStep(player2);

        assertThat(warriorTokens(player1)).isEmpty();
    }

    @Test
    void protectedMobilizeTokensAreNotSacrificedAtLaterEndSteps() {
        Permanent zurgo = addCreatureReady(player1, new ZurgoThundersDecree());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        List<Permanent> tokens = warriorTokens(player1);
        assertThat(tokens).hasSize(2);

        advanceToEndStep(player1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, zurgo);
        advanceToEndStep(player2);

        assertThat(warriorTokens(player1)).containsExactlyInAnyOrderElementsOf(tokens);
    }

    @Test
    void mobilizeTokensAreSacrificedIfZurgoLeavesBeforeYourEndStep() {
        Permanent zurgo = addCreatureReady(player1, new ZurgoThundersDecree());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(warriorTokens(player1)).hasSize(2);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, zurgo);
        advanceToEndStep(player1);

        assertThat(warriorTokens(player1)).isEmpty();
    }

    @Test
    void sacrificeProtectionAppliesOnlyDuringYourEndStepAndOnlyToTokens() {
        Permanent zurgo = addCreatureReady(player1, new ZurgoThundersDecree());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        List<Permanent> tokens = warriorTokens(player1);
        assertThat(tokens).hasSize(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThat(tokens).allSatisfy(token -> assertThat(gqs.cantBeSacrificed(gd, token)).isFalse());
        advanceToEndStep(player1);
        assertThat(tokens).allSatisfy(token -> assertThat(gqs.cantBeSacrificed(gd, token)).isTrue());
        assertThat(gqs.cantBeSacrificed(gd, zurgo)).isFalse();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        assertThat(tokens).allSatisfy(token -> assertThat(gqs.cantBeSacrificed(gd, token)).isFalse());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.CLEANUP);
        assertThat(tokens).allSatisfy(token -> assertThat(gqs.cantBeSacrificed(gd, token)).isFalse());
    }

    @Test
    @CardUsed({ZurgoThundersDecree.class, ObsidianBattleAxe.class, SaheelisArtistry.class})
    void noncreatureWarriorTokensCannotBeSacrificedDuringYourEndStep() {
        addCreatureReady(player1, new ZurgoThundersDecree());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new ObsidianBattleAxe());
        harness.setHand(player1, List.of(new SaheelisArtistry()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(axe.getId()), null);
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Obsidian Battle-Axe").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        advanceToEndStep(player1);

        assertThat(gqs.cantBeSacrificed(gd, token)).isTrue();
        assertThat(gqs.cantBeSacrificed(gd, axe)).isFalse();
    }

    private List<Permanent> warriorTokens(Player player) {
        return findPermanents(player, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
