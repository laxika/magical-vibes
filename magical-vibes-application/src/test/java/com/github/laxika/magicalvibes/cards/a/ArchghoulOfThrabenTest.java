package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArchghoulOfThrabenTest extends BaseCardTest {

    @Test
    @DisplayName("Another Zombie dies with Zombie on top — accept reveals to hand")
    void anotherZombieDiesMatchingAcceptToHand() {
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        harness.addToBattlefield(player1, new DiregrafGhoul());

        Card topZombie = new DiregrafGhoul();
        gd.playerDecks.get(player1.getId()).addFirst(topZombie);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        killPlayer1Creature("Diregraf Ghoul");
        harness.passBothPriorities(); // resolve look trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topZombie.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topZombie.getId()));
    }

    @Test
    @DisplayName("Another Zombie dies with Zombie on top — decline hand then accept graveyard")
    void anotherZombieDiesMatchingDeclineHandAcceptGraveyard() {
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        harness.addToBattlefield(player1, new DiregrafGhoul());

        Card topZombie = new DiregrafGhoul();
        gd.playerDecks.get(player1.getId()).addFirst(topZombie);

        killPlayer1Creature("Diregraf Ghoul");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false); // decline hand
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true); // accept graveyard

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topZombie.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topZombie.getId()));
    }

    @Test
    @DisplayName("Another Zombie dies with non-Zombie on top — may put into graveyard")
    void anotherZombieDiesNonMatchingMayGraveyard() {
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        harness.addToBattlefield(player1, new DiregrafGhoul());

        Card topBear = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topBear);

        killPlayer1Creature("Diregraf Ghoul");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topBear.getId()));
    }

    @Test
    @DisplayName("Another Zombie dies with non-Zombie on top — decline leaves card on top")
    void anotherZombieDiesNonMatchingDeclineLeavesOnTop() {
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        harness.addToBattlefield(player1, new DiregrafGhoul());

        Card topBear = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topBear);

        killPlayer1Creature("Diregraf Ghoul");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topBear.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topBear.getId()));
    }

    @Test
    @DisplayName("Non-Zombie death does not trigger")
    void nonZombieDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Card topZombie = new DiregrafGhoul();
        gd.playerDecks.get(player1.getId()).addFirst(topZombie);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        killPlayer1Creature("Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topZombie.getId());
    }

    @Test
    @DisplayName("Own death triggers the look ability")
    void ownDeathTriggers() {
        harness.addToBattlefield(player1, new ArchghoulOfThraben());

        Card topZombie = new DiregrafGhoul();
        gd.playerDecks.get(player1.getId()).addFirst(topZombie);

        killPlayer1CreatureWithBolt("Archghoul of Thraben");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topZombie.getId()));
    }

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        harness.addToBattlefield(player1, new DiregrafGhoul());
        gd.playerDecks.get(player1.getId()).clear();

        killPlayer1Creature("Diregraf Ghoul");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void killPlayer1Creature(String name) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID id = harness.getPermanentId(player1, name);
        harness.castInstant(player2, 0, id);
        harness.passBothPriorities();
    }

    private void killPlayer1CreatureWithBolt(String name) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID id = harness.getPermanentId(player1, name);
        harness.castInstant(player2, 0, id);
        harness.passBothPriorities();
    }
}
