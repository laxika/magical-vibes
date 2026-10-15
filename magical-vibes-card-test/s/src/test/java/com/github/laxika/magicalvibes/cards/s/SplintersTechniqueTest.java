package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PurpleDragonPunks;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplintersTechnique.class, PurpleDragonPunks.class})
class SplintersTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Searches the library for any card when cast normally")
    void searchesLibraryWhenCastNormally() {
        Card technique = new SplintersTechnique();
        Card chosenCard = new PurpleDragonPunks();
        harness.setHand(player1, List.of(technique));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(chosenCard));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        chooseLibraryCard(chosenCard);

        harness.assertInHand(player1, "Purple Dragon Punks");
        harness.assertInGraveyard(player1, "Splinter's Technique");
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker before searching the library")
    void sneakReturnsUnblockedAttackerAndSearches() {
        Permanent attacker = addCreatureReady(player1, new PurpleDragonPunks());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Card chosenCard = new PurpleDragonPunks();
        harness.setHand(player1, List.of(new SplintersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(chosenCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        chooseLibraryCard(chosenCard);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Purple Dragon Punks", "Purple Dragon Punks");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SplintersTechnique);
    }

    @Test
    void canSearchForANoncreatureCard() {
        Card chosenCard = new SplintersTechnique();
        harness.setHand(player1, List.of(new SplintersTechnique()));
        harness.setLibrary(player1, List.of(new PurpleDragonPunks(), chosenCard));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        chooseLibraryCard(chosenCard);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Splinter's Technique");
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        harness.setHand(player1, List.of(new SplintersTechnique()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Splinter's Technique");
    }

    @Test
    void cannotPayNormalCostDuringDeclareBlockers() {
        harness.setHand(player1, List.of(new SplintersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Splinter's Technique");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sneakCannotReturnABlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new PurpleDragonPunks());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new PurpleDragonPunks());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        harness.setHand(player1, List.of(new SplintersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInHand(player1, "Splinter's Technique");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sneakCannotBeUsedDuringDeclareAttackers() {
        Permanent attacker = addCreatureReady(player1, new PurpleDragonPunks());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new SplintersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInHand(player1, "Splinter's Technique");
        assertThat(gd.stack).isEmpty();
    }

    private void chooseLibraryCard(Card chosenCard) {
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();

        int chosenIndex = search.params().cards().indexOf(chosenCard);
        harness.handleCardChosen(player1, chosenIndex);
    }

}
