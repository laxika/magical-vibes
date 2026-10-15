package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CodexShredder;
import com.github.laxika.magicalvibes.cards.c.Cremate;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorldspineWurm.class, CodexShredder.class, Cremate.class, MindRot.class})
class WorldspineWurmTest extends BaseCardTest {

    @Test
    @DisplayName("When Worldspine Wurm dies it creates three 5/5 trampling Wurm tokens")
    void deathCreatesThreeWurmTokens() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new WorldspineWurm());
        wurm.setMarkedDamage(15);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Wurm");

        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(5);
            assertThat(token.getCard().getToughness()).isEqualTo(5);
            assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        });
    }

    @Test
    @DisplayName("When Worldspine Wurm is put into a graveyard it is shuffled into its owner's library")
    void deathShufflesItselfIntoLibrary() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new WorldspineWurm());
        wurm.setMarkedDamage(15);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Worldspine Wurm");
        harness.assertInGraveyard(player1, "Worldspine Wurm");

        // Death creates tokens and the from-anywhere shuffle both trigger; resolve both.
        resolveAllTriggers();

        harness.assertNotInGraveyard(player1, "Worldspine Wurm");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Worldspine Wurm"));
    }

    @Test
    @DisplayName("Milling Worldspine Wurm triggers its shuffle ability without creating tokens")
    void millingShufflesWithoutTokens() {
        WorldspineWurm wurm = new WorldspineWurm();
        harness.setLibrary(player2, List.of(wurm));
        harness.addToBattlefield(player1, new CodexShredder());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Worldspine Wurm");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(wurm);
        harness.assertNotInGraveyard(player2, "Worldspine Wurm");
        assertThat(countPermanents(player1, "Wurm")).isZero();
        assertThat(countPermanents(player2, "Wurm")).isZero();
    }

    @Test
    @DisplayName("Discarding Worldspine Wurm triggers its shuffle ability without creating tokens")
    void discardShufflesWithoutTokens() {
        WorldspineWurm wurm = new WorldspineWurm();
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(wurm));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Worldspine Wurm");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(wurm);
        harness.assertNotInGraveyard(player2, "Worldspine Wurm");
        assertThat(countPermanents(player2, "Wurm")).isZero();
    }

    @Test
    @DisplayName("Exiling the dead Wurm in response prevents its return but not its tokens")
    void exileInResponseDoesNotStopDeathTokens() {
        harness.setLibrary(player2, List.of());
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        harness.setHand(player1, List.of(new Cremate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        wurm.setMarkedDamage(15);
        harness.runStateBasedActions();

        harness.castAndResolveInstant(player1, 0, wurm.getCard().getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(wurm.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player2, "Worldspine Wurm");
        assertThat(countPermanents(player2, "Wurm")).isEqualTo(3);
        assertThat(countPermanents(player1, "Wurm")).isZero();
    }

    @Test
    @DisplayName("Losing abilities before death stops tokens but not the graveyard shuffle trigger")
    void abilityLossStopsDeathTriggerOnly() {
        harness.setLibrary(player1, List.of());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new WorldspineWurm());
        wurm.setLosesAllAbilitiesUntilEndOfTurn(true);
        wurm.setMarkedDamage(15);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Worldspine Wurm");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wurm.getCard());
        harness.assertNotInGraveyard(player1, "Worldspine Wurm");
        assertThat(countPermanents(player1, "Wurm")).isZero();
    }
}
