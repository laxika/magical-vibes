package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CenoteScout;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlowcapLantern.class, Forest.class, CenoteScout.class})
class GlowcapLanternTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping and attacking explores with a land on top")
    void equippedCreatureExploresLandIntoHand() {
        Permanent creature = addCreatureReady(player1, new CenoteScout());
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new GlowcapLantern());
        lantern.setAttachedTo(creature.getId());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Equipped creature explores a nonland and may put it into the graveyard")
    void equippedCreatureExploresNonland() {
        Permanent creature = addCreatureReady(player1, new CenoteScout());
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new GlowcapLantern());
        lantern.setAttachedTo(creature.getId());
        Card nonland = new CenoteScout();
        harness.setLibrary(player1, List.of(nonland));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(nonland);
    }

    @Test
    @DisplayName("An unattached lantern does not grant explore")
    void unattachedLanternDoesNotTrigger() {
        addCreatureReady(player1, new CenoteScout());
        harness.addToBattlefieldAndReturn(player1, new GlowcapLantern());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void equipForTwoManaGrantsAttackExplore() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new GlowcapLantern());
        Permanent creature = addCreatureReady(player1, new CenoteScout());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(lantern.getAttachedTo()).isEqualTo(creature.getId());
        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    void mayKeepNonlandOnTop() {
        Permanent creature = addCreatureReady(player1, new CenoteScout());
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new GlowcapLantern());
        lantern.setAttachedTo(creature.getId());
        Card nonland = new GlowcapLantern();
        harness.setLibrary(player1, List.of(nonland));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nonland);
    }

    @Test
    void emptyLibraryStillGivesExploreCounter() {
        Permanent creature = addCreatureReady(player1, new CenoteScout());
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new GlowcapLantern());
        lantern.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackTriggerSurvivesLanternLeavingBattlefield() {
        Permanent creature = addCreatureReady(player1, new CenoteScout());
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new GlowcapLantern());
        lantern.setAttachedTo(creature.getId());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(lantern);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    void attachedLanternAllowsPrivateLookAtTopCard() {
        Permanent creature = addCreatureReady(player1, new CenoteScout());
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new GlowcapLantern());
        lantern.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GlowcapLantern()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Forest")
                        && message.contains("}],[]]"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void equippedCreatureLetsOnlyItsControllerLookAtLibraryTop() {
        Permanent creature = addCreatureReady(player1, new CenoteScout());
        Permanent lantern = harness.addToBattlefieldAndReturn(player2, new GlowcapLantern());
        lantern.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GlowcapLantern()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Forest")
                        && message.contains("}],[]]"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));

        lantern.setAttachedTo(null);
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }
}
