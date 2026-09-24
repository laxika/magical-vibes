package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HanskSlayerZealot.class, GrizzlyBears.class, DiregrafGhoul.class, Shock.class})
class HanskSlayerZealotTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, target opponent creates three Walker tokens")
    void upkeepCreatesWalkersForTargetOpponent() {
        addCreatureReady(player1, new HanskSlayerZealot());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        List<Permanent> walkers = findPermanents(player2, "Walker");
        assertThat(walkers).hasSize(3);
        assertThat(walkers).allSatisfy(walker ->
                assertThat(walker.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE));
    }

    @Test
    @DisplayName("The tap ability deals 2 damage to a target creature")
    void tapAbilityDealsTwoDamage() {
        Permanent hansk = addCreatureReady(player1, new HanskSlayerZealot());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(hansk.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draws a card when a Zombie an opponent controls dies")
    void drawsWhenOpponentZombieDies() {
        addCreatureReady(player1, new HanskSlayerZealot());
        Permanent zombie = addCreatureReady(player2, new DiregrafGhoul());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player1, 0, zombie.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    @DisplayName("Does not draw when a non-Zombie opponent creature dies")
    void doesNotDrawWhenOpponentNonZombieDies() {
        addCreatureReady(player1, new HanskSlayerZealot());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Gravedigger()));
        harness.setHand(player1, List.of(new Shock()));

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when your own Zombie dies")
    void doesNotDrawWhenOwnZombieDies() {
        addCreatureReady(player1, new HanskSlayerZealot());
        Permanent zombie = addCreatureReady(player1, new DiregrafGhoul());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player2, 0, zombie.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
