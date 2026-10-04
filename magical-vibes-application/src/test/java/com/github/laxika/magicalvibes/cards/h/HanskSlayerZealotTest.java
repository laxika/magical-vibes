package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HanskSlayerZealot.class, GrizzlyBears.class, ScatheZombies.class, AmoeboidChangeling.class})
class HanskSlayerZealotTest extends BaseCardTest {

    @Test
    @DisplayName("At upkeep, target opponent creates three Walker Zombie tokens")
    void createsThreeWalkerTokensForTargetOpponent() {
        addReadyHansk();

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        List<Permanent> walkers = findPermanents(player2, "Walker");
        assertThat(walkers).hasSize(3);
        assertThat(walkers).allMatch(walker -> walker.getCard().isToken()
                && walker.getCard().getPower() == 2
                && walker.getCard().getToughness() == 2
                && walker.getCard().getSubtypes().contains(CardSubtype.ZOMBIE));
    }

    @Test
    @DisplayName("Tap ability deals two damage to a target creature")
    void dealsTwoDamageToTargetCreature() {
        Permanent hansk = addReadyHansk();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hansk), null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draws a card when an opponent's Zombie dies")
    void drawsWhenOpponentsZombieDies() {
        Permanent hansk = addReadyHansk();
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        Permanent walker = findPermanents(player2, "Walker").getFirst();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hansk), null, walker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Does not create Walkers during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        addReadyHansk();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Walker")).isEmpty();
        assertThat(findPermanents(player2, "Walker")).isEmpty();
    }

    @Test
    @DisplayName("Draws when an opponent's nontoken Zombie dies")
    void drawsForNontokenZombie() {
        Permanent hansk = addReadyHansk();
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hansk), null, zombie.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Scathe Zombies");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw when its controller's Zombie dies")
    void doesNotDrawForOwnZombie() {
        Permanent hansk = addReadyHansk();
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new ScatheZombies());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hansk), null, zombie.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scathe Zombies");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when an opponent's non-Zombie dies")
    void doesNotDrawForOpponentsNonZombie() {
        Permanent hansk = addReadyHansk();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hansk), null, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws for a creature that gained Zombie before dying")
    void drawsForTemporarilyGrantedZombieType() {
        Permanent hansk = addReadyHansk();
        Permanent changeling = addCreatureReady(player1, new AmoeboidChangeling());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(changeling),
                0, null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.effectiveCreatureSubtypes(gd, bear)).contains(CardSubtype.ZOMBIE);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hansk), null, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw for a Zombie that lost its creature types before dying")
    void doesNotDrawForRemovedZombieType() {
        Permanent hansk = addReadyHansk();
        Permanent changeling = addCreatureReady(player1, new AmoeboidChangeling());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(changeling),
                1, null, zombie.getId());
        harness.passBothPriorities();
        assertThat(gqs.effectiveCreatureSubtypes(gd, zombie)).doesNotContain(CardSubtype.ZOMBIE);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hansk), null, zombie.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Scathe Zombies");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyHansk() {
        return addCreatureReady(player1, new HanskSlayerZealot());
    }
}
