package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecromasterDragon.class, ColossodonYearling.class})
class NecromasterDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} creates a Zombie token and makes each opponent mill two cards")
    void payingCreatesTokenAndMillsEachOpponent() {
        addAttackingDragon();
        harness.setLibrary(player2, List.of(new ColossodonYearling(), new ColossodonYearling(), new ColossodonYearling()));

        resolveCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the payment creates no token and mills no cards")
    void decliningDoesNothing() {
        addAttackingDragon();
        harness.setLibrary(player2, List.of(new ColossodonYearling(), new ColossodonYearling(), new ColossodonYearling()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Accepting without enough mana creates no token and mills no cards")
    void insufficientManaDoesNothing() {
        addAttackingDragon();
        harness.setLibrary(player2, List.of(new ColossodonYearling(), new ColossodonYearling()));

        resolveCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Payment creates a token even when the opponent has fewer than two cards")
    void shortLibraryStillCreatesToken() {
        addAttackingDragon();
        ColossodonYearling lastCard = new ColossodonYearling();
        harness.setLibrary(player2, List.of(lastCard));
        ColossodonYearling controllerCard = new ColossodonYearling();
        harness.setLibrary(player1, List.of(controllerCard));

        resolveCombat();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(lastCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The combat damage trigger resolves after the Dragon leaves the battlefield")
    void triggerResolvesWithoutSource() {
        addAttackingDragon();
        harness.setLibrary(player2, List.of(new ColossodonYearling(), new ColossodonYearling()));

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Necromaster Dragon"));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private void addAttackingDragon() {
        Permanent dragon = addCreatureReady(player1, new NecromasterDragon());
        dragon.setAttacking(true);
    }
}
