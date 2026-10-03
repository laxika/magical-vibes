package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UnnaturalSelection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchghoulOfThraben.class, DiregrafGhoul.class, GrizzlyBears.class,
        Shock.class, LightningBolt.class, UnnaturalSelection.class})
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
        harness.setLibrary(player1, List.of());

        killPlayer1Creature("Diregraf Ghoul");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining both choices leaves a Zombie on top")
    void matchingDeclineBothLeavesOnTop() {
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        harness.addToBattlefield(player1, new DiregrafGhoul());
        Card topZombie = new ArchghoulOfThraben();
        harness.setLibrary(player1, List.of(topZombie));

        killPlayer1Creature("Diregraf Ghoul");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topZombie);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topZombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topZombie);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's Zombie dying does not trigger")
    void opponentsZombieDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        harness.addToBattlefield(player2, new DiregrafGhoul());
        Card topZombie = new ArchghoulOfThraben();
        harness.setLibrary(player1, List.of(topZombie));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Diregraf Ghoul"));

        harness.assertInGraveyard(player2, "Diregraf Ghoul");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topZombie);
    }

    @Test
    @DisplayName("Simultaneous deaths trigger once for self and once for another Zombie")
    void simultaneousDeathsTriggerForEachZombie() {
        Permanent archghoul = harness.addToBattlefieldAndReturn(player1, new ArchghoulOfThraben());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new DiregrafGhoul());
        Card first = new ArchghoulOfThraben();
        Card second = new ArchghoulOfThraben();
        harness.setLibrary(player1, List.of(first, second));

        archghoul.addMarkedDamage(null, 2);
        zombie.addMarkedDamage(null, 2);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature made into a Zombie triggers on death")
    void creatureMadeIntoZombieTriggers() {
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topZombie = new ArchghoulOfThraben();
        harness.setLibrary(player1, List.of(topZombie));
        changeCreatureType(bear, CardSubtype.ZOMBIE);

        killPlayer1Creature("Grizzly Bears");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(topZombie);
    }

    @Test
    @DisplayName("A Zombie changed into a non-Zombie does not trigger on death")
    void zombieChangedIntoNonZombieDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new DiregrafGhoul());
        Card topZombie = new ArchghoulOfThraben();
        harness.setLibrary(player1, List.of(topZombie));
        changeCreatureType(zombie, CardSubtype.BEAR);

        killPlayer1Creature("Diregraf Ghoul");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topZombie);
    }

    @Test
    @DisplayName("Own death still triggers after losing the Zombie subtype")
    void ownDeathTriggersWithoutZombieSubtype() {
        Permanent archghoul = harness.addToBattlefieldAndReturn(player1, new ArchghoulOfThraben());
        Card topZombie = new ArchghoulOfThraben();
        harness.setLibrary(player1, List.of(topZombie));
        changeCreatureType(archghoul, CardSubtype.BEAR);

        killPlayer1Creature("Archghoul of Thraben");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(topZombie);
        assertThat(gd.stack).isEmpty();
    }

    private void changeCreatureType(Permanent target, CardSubtype subtype) {
        Permanent selection = harness.addToBattlefieldAndReturn(player1, new UnnaturalSelection());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(selection);
        harness.activateAbility(player1, index, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());
    }

    private void killPlayer1Creature(String name) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID id = harness.getPermanentId(player1, name);
        harness.castAndResolveInstant(player2, 0, id);
    }

    private void killPlayer1CreatureWithBolt(String name) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID id = harness.getPermanentId(player1, name);
        harness.castAndResolveInstant(player2, 0, id);
    }
}
