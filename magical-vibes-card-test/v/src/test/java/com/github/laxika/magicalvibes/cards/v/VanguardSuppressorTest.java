package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VanguardSuppressor.class})
class VanguardSuppressorTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        harness.setHand(player1, List.of(new VanguardSuppressor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}", "{2}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vanguard Suppressor")).hasSize(3);
        assertThat(findPermanents(player1, "Vanguard Suppressor"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Deals combat damage to a player and draws a card")
    void combatDamageToPlayerDrawsCard() {
        Permanent suppressor = addCreatureReady(player1, new VanguardSuppressor());
        suppressor.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new VanguardSuppressor()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting without paying squad creates no copies")
    void unpaidSquadCreatesNoCopies() {
        harness.setHand(player1, List.of(new VanguardSuppressor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vanguard Suppressor")).hasSize(1);
        assertThat(findPermanent(player1, "Vanguard Suppressor").getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("Squad copies do not repeat paid squad and each draws on combat damage")
    void squadCopiesEachDrawOnCombatDamage() {
        harness.setHand(player1, List.of(new VanguardSuppressor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.setLibrary(player1, List.of(
                new VanguardSuppressor(), new VanguardSuppressor(), new VanguardSuppressor()));

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}", "{2}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vanguard Suppressor")).hasSize(3);
        for (Permanent suppressor : findPermanents(player1, "Vanguard Suppressor")) {
            suppressor.setSummoningSick(false);
            suppressor.setAttacking(true);
        }
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not draw a card")
    void combatDamageToCreatureDoesNotDraw() {
        Permanent attacker = addCreatureReady(player1, new VanguardSuppressor());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new VanguardSuppressor());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new VanguardSuppressor()));
        harness.setLibrary(player2, List.of(new VanguardSuppressor()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The attacking creature's controller draws when the opponent attacks")
    void opposingControllerDrawsCard() {
        Permanent suppressor = addCreatureReady(player2, new VanguardSuppressor());
        suppressor.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new VanguardSuppressor()));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
