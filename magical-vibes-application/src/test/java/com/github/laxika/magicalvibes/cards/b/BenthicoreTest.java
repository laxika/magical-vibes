package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Benthicore.class, DeeptreadMerrow.class, Tarfire.class})
class BenthicoreTest extends BaseCardTest {

    // ===== ETB: creates two Merfolk Wizard tokens =====

    @Test
    @DisplayName("ETB creates two 1/1 blue Merfolk Wizard tokens")
    void etbCreatesTwoMerfolkWizardTokens() {
        castAndResolveBenthicore();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(countMerfolkWizardTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Merfolk Wizard tokens are 1/1 with Merfolk and Wizard subtypes")
    void merfolkWizardTokensHaveCorrectStats() {
        castAndResolveBenthicore();

        Permanent token = findMerfolkWizardToken(player1);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.MERFOLK, CardSubtype.WIZARD);
    }

    // ===== Activated ability =====

    @Nested
    @DisplayName("Activated ability")
    class ActivatedAbilityTests {

        @Test
        @DisplayName("Activating ability puts it on the stack")
        void activatingPutsOnStack() {
            Permanent benthicore = addBenthicoreReady(player1);
            addMerfolk(player1, 2);

            harness.activateAbility(player1, battlefieldIndex(benthicore), null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
            assertThat(gd.stack.getFirst().getCard()).isSameAs(benthicore.getCard());
        }

        @Test
        @DisplayName("Resolving ability untaps Benthicore and grants shroud until end of turn")
        void resolvingUntapsAndGrantsShroud() {
            Permanent benthicore = addBenthicoreReady(player1);
            benthicore.tap();
            addMerfolk(player1, 2);

            harness.activateAbility(player1, battlefieldIndex(benthicore), null, null);
            resolveAllTriggers();

            assertThat(benthicore.isTapped()).isFalse();
            assertThat(gqs.hasKeyword(gd, benthicore, Keyword.SHROUD)).isTrue();
        }

        @Test
        @DisplayName("Shroud wears off at end of turn cleanup")
        void shroudWearsOffAtEndOfTurn() {
            Permanent benthicore = addBenthicoreReady(player1);
            addMerfolk(player1, 2);

            harness.activateAbility(player1, battlefieldIndex(benthicore), null, null);
            resolveAllTriggers();
            assertThat(gqs.hasKeyword(gd, benthicore, Keyword.SHROUD)).isTrue();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, benthicore, Keyword.SHROUD)).isFalse();
        }

        @Test
        @DisplayName("Tapping two Merfolk taps them as cost")
        void tapsMerfolkAsCost() {
            Permanent benthicore = addBenthicoreReady(player1);
            addMerfolk(player1, 3);

            harness.activateAbility(player1, battlefieldIndex(benthicore), null, null);
            tapMerfolk(player1, 2);

            long tappedMerfolk = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.MERFOLK))
                    .filter(Permanent::isTapped)
                    .count();
            assertThat(tappedMerfolk).isEqualTo(2);

            long untappedMerfolk = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.MERFOLK))
                    .filter(p -> !p.isTapped())
                    .count();
            assertThat(untappedMerfolk).isEqualTo(1);
        }

        @Test
        @DisplayName("Cannot activate with fewer than two untapped Merfolk")
        void cannotActivateWithFewerThanTwoMerfolk() {
            Permanent benthicore = addBenthicoreReady(player1);
            addMerfolk(player1, 1);

            assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(benthicore), null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Tapped Merfolk cannot pay the activation cost")
        void tappedMerfolkCannotPayCost() {
            Permanent benthicore = addBenthicoreReady(player1);
            Permanent firstMerfolk = addCreatureReady(player1, new DeeptreadMerrow());
            Permanent secondMerfolk = addCreatureReady(player1, new DeeptreadMerrow());
            firstMerfolk.tap();

            assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(benthicore), null, null))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(secondMerfolk.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Only Merfolk controlled by the activating player can pay the cost")
        void opponentMerfolkCannotPayCost() {
            Permanent benthicore = addBenthicoreReady(player1);
            addCreatureReady(player1, new DeeptreadMerrow());
            addCreatureReady(player2, new DeeptreadMerrow());

            assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(benthicore), null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Non-Merfolk creatures cannot be tapped to pay the cost")
        void nonMerfolkCannotBeUsed() {
            Permanent benthicore = addBenthicoreReady(player1);
            addMerfolk(player1, 1);
            addBenthicoreReady(player1);

            assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(benthicore), null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Shroud prevents a targeted spell from targeting Benthicore")
        void shroudPreventsTargeting() {
            Permanent benthicore = addBenthicoreReady(player1);
            addMerfolk(player1, 2);

            harness.activateAbility(player1, battlefieldIndex(benthicore), null, null);
            resolveAllTriggers();

            harness.setHand(player1, List.of(new Tarfire()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, benthicore.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ===== Helpers =====

    private void castAndResolveBenthicore() {
        harness.castFromHand(player1, new Benthicore(), "{6}{U}");
        resolveAllTriggers();
    }

    private Permanent addBenthicoreReady(Player player) {
        return addCreatureReady(player, new Benthicore());
    }

    private void addMerfolk(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new DeeptreadMerrow());
        }
    }

    private int countMerfolkWizardTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Merfolk Wizard"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.MERFOLK))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.WIZARD))
                .count();
    }

    private Permanent findMerfolkWizardToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Merfolk Wizard"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Merfolk Wizard token found"));
    }

    private void tapMerfolk(Player player, int count) {
        List<Permanent> untappedMerfolk = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.MERFOLK))
                .filter(p -> !p.isTapped())
                .limit(count)
                .toList();
        for (Permanent merfolk : untappedMerfolk) {
            harness.handlePermanentChosen(player, merfolk.getId());
        }
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
