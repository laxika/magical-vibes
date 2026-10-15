package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LodestoneGolem;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AloySaviorOfMeridian.class, GrizzlyBears.class, HillGiant.class, LodestoneGolem.class,
        Memnite.class, Plains.class, GiantGrowth.class, Unsummon.class})
class AloySaviorOfMeridianTest extends BaseCardTest {

    @Test
    @DisplayName("Discovers using the greatest power among attacking artifact creatures")
    void discoversUsingGreatestAttackingArtifactCreaturePower() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Plains(), discovered));
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        addCreatureReady(player1, new LodestoneGolem());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    @DisplayName("Ignores non-artifact attackers when determining discover X")
    void ignoresNonArtifactAttackers() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Plains(), discovered));
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new Memnite());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
    }

    @Test
    @DisplayName("Does not trigger when no artifact creature attacks")
    void doesNotTriggerWithoutArtifactCreatureAttacker() {
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void triggersOnlyOnceForMultipleArtifactAttackersAndUsesTheirGreatestPower() {
        HillGiant discovered = new HillGiant();
        harness.setLibrary(player1, List.of(new Plains(), discovered));
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        addCreatureReady(player1, new Memnite());
        addCreatureReady(player1, new LodestoneGolem());

        declareAttackers(List.of(1, 2));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    void ignoresArtifactCreaturesThatDidNotAttackAndBottomsSkippedCards() {
        Plains land = new Plains();
        GrizzlyBears tooExpensive = new GrizzlyBears();
        Memnite discovered = new Memnite();
        harness.setLibrary(player1, List.of(land, tooExpensive, discovered));
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        addCreatureReady(player1, new Memnite());
        addCreatureReady(player1, new LodestoneGolem());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered).doesNotContain(tooExpensive);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, tooExpensive);
    }

    @Test
    void canCastTheDiscoveredArtifactWithoutPayingMana() {
        LodestoneGolem discovered = new LodestoneGolem();
        harness.setLibrary(player1, List.of(discovered));
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        addCreatureReady(player1, new LodestoneGolem());

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == discovered);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
    }

    @Test
    void doesNotTriggerForAnOpponentsArtifactAttackers() {
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        addCreatureReady(player2, new Memnite());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesPowerAtResolutionAfterAnAttackerIsPumped() {
        HillGiant discovered = new HillGiant();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        var attacker = addCreatureReady(player1, new Memnite());

        declareAttackers(List.of(1));
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);
    }

    @Test
    void stillDiscoversWhenTheOnlyArtifactAttackerLeavesBeforeResolution() {
        HillGiant discovered = new HillGiant();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        var attacker = addCreatureReady(player1, new LodestoneGolem());

        declareAttackers(List.of(1));
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);
    }

    @Test
    void usesLastKnownPowerOfTheGreatestArtifactAttackerWhenItLeaves() {
        HillGiant discovered = new HillGiant();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        var attacker = addCreatureReady(player1, new LodestoneGolem());
        addCreatureReady(player1, new Memnite());

        declareAttackers(List.of(1, 2));
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);
    }
}
