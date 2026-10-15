package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.p.PossibilityStorm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreakingEntering.class, KraulWarrior.class, PossibilityStorm.class})
class BreakingEnteringTest extends BaseCardTest {

    private static final int BREAKING = 0;
    private static final int ENTERING = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Breaking mills eight cards from the target player's library")
    void breakingMillsEightCards() {
        harness.setLibrary(player2, List.of(
                new KraulWarrior(), new KraulWarrior(), new KraulWarrior(), new KraulWarrior(),
                new KraulWarrior(), new KraulWarrior(), new KraulWarrior(), new KraulWarrior(),
                new KraulWarrior()));
        harness.setHand(player1, List.of(new BreakingEntering()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, BREAKING, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Entering returns a creature from any graveyard with haste")
    void enteringReturnsCreatureWithHaste() {
        Card target = new KraulWarrior();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new BreakingEntering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, ENTERING, (UUID) null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.getCard()).isSameAs(target);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player2, "Kraul Warrior");

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(null, TurnStep.CLEANUP);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Fuse resolves Breaking before Entering")
    void fuseMillsThenReturnsCreatureWithHaste() {
        harness.setLibrary(player2, List.of(
                new KraulWarrior(), new KraulWarrior(), new KraulWarrior(), new KraulWarrior(),
                new KraulWarrior(), new KraulWarrior(), new KraulWarrior(), new KraulWarrior(),
                new KraulWarrior()));
        Card target = gd.playerDecks.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new BreakingEntering()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, FUSE, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        PendingInteraction.GraveyardChoice choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.cardPool()).contains(target);
        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(target));
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.getCard()).isSameAs(target);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
    }

    @Test
    void breakingCanMillItsControllerAndAnUndersizedLibrary() {
        Card first = new KraulWarrior();
        Card second = new KraulWarrior();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new BreakingEntering()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, BREAKING, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringChoosesOnlyCreaturesAndCanUseItsControllersGraveyard() {
        Card creature = new KraulWarrior();
        Card sorcery = new BreakingEntering();
        harness.setGraveyard(player1, List.of(sorcery, creature));
        harness.setGraveyard(player2, List.of(new BreakingEntering()));
        harness.setHand(player1, List.of(new BreakingEntering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, ENTERING, (UUID) null);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.cardPool()).containsExactly(creature);
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.getCard()).isSameAs(creature);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sorcery).doesNotContain(creature);
    }

    @Test
    void enteringResolvesWithoutCreaturesInAnyGraveyard() {
        harness.setGraveyard(player1, List.of(new BreakingEntering()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new BreakingEntering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, ENTERING, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void fuseIsNotOfferedWhenPossibilityStormCastsTheCardFromExile() {
        harness.addToBattlefield(player1, new PossibilityStorm());
        harness.setLibrary(player1, List.of(new BreakingEntering()));
        harness.setHand(player1, List.of(new BreakingEntering()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, BREAKING, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).hasSize(2);
        assertThat(choice.options()).noneMatch(label -> label.startsWith("Fuse"));
    }
}
