package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NissaStewardOfElementsTest extends BaseCardTest {

    // ===== Entering with X loyalty =====

    @Test
    @DisplayName("Enters the battlefield with loyalty counters equal to X paid")
    void entersWithXLoyalty() {
        harness.setHand(player1, List.of(new NissaStewardOfElements()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castPlaneswalker(player1, 0, 3); // {X}{G}{U} with X=3
        harness.passBothPriorities();

        Permanent nissa = findPermanent(player1, "Nissa, Steward of Elements");
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    // ===== +2: Scry 2 =====

    @Test
    @DisplayName("+2 raises loyalty by two and scries 2")
    void plusTwoScries() {
        Permanent nissa = addReadyNissa(player1, 3);
        harness.setLibrary(player1, deckOf(new Forest(), new GrizzlyBears(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        // Keep both on top to finish the interaction.
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    // ===== 0: Look at the top card =====

    @Test
    @DisplayName("0 may put a land from the top of library onto the battlefield")
    void zeroPutsLand() {
        addReadyNissa(player1, 3);
        harness.setLibrary(player1, deckOf(new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("0 may put a creature with mana value <= loyalty onto the battlefield")
    void zeroPutsLowCostCreature() {
        addReadyNissa(player1, 3); // Grizzly Bears MV 2 <= 3
        harness.setLibrary(player1, deckOf(new GrizzlyBears(), new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("0 offers no choice when the top creature's mana value exceeds loyalty")
    void zeroCreatureTooExpensive() {
        addReadyNissa(player1, 1); // Grizzly Bears MV 2 > 1
        harness.setLibrary(player1, deckOf(new GrizzlyBears(), new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).get(0).getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("0 leaves the card on top when declined")
    void zeroDeclined() {
        addReadyNissa(player1, 3);
        harness.setLibrary(player1, deckOf(new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).get(0).getName()).isEqualTo("Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    // ===== -6: Untap up to two target lands, animate them =====

    @Test
    @DisplayName("-6 untaps and animates two target lands into 5/5 fliers with haste that are still lands")
    void minusSixAnimatesLands() {
        Permanent nissa = addReadyNissa(player1, 7);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        forest1.tap();
        forest2.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(forest1.getId(), forest2.getId()));
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        for (Permanent land : List.of(forest1, forest2)) {
            assertThat(land.isTapped()).isFalse();
            assertThat(gqs.isCreature(gd, land)).isTrue();
            assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(5);
            assertThat(gqs.hasKeyword(gd, land, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
            assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
        }
    }

    @Test
    @DisplayName("-6 animation wears off at end of turn")
    void minusSixWearsOff() {
        addReadyNissa(player1, 7);
        Permanent forest = addForest(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(forest.getId()));
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("-6 cannot target a land you don't control")
    void minusSixCannotTargetOpponentLand() {
        addReadyNissa(player1, 7);
        Permanent oppForest = addForest(player2);

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(oppForest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent perm = new Permanent(new NissaStewardOfElements());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addForest(Player player) {
        Permanent perm = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
