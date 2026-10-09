package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorpseHarvester.class, GrizzlyBears.class, GravebaneZombie.class, Swamp.class, Forest.class,
        PsychogenicProbe.class})
class CorpseHarvesterTest extends BaseCardTest {

    @Test
    void searchesForAZombieAndASwamp() {
        addCreatureReady(player1, new CorpseHarvester());
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLibrary(player1, List.of(new GravebaneZombie(), new Swamp(), new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch zombieSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(zombieSearch).isNotNull();
        assertThat(zombieSearch.params().cards()).allMatch(card -> card instanceof GravebaneZombie);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        PendingInteraction.LibrarySearch swampSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(swampSearch).isNotNull();
        assertThat(swampSearch.params().cards()).allMatch(card -> card instanceof Swamp);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GravebaneZombie)
                .anyMatch(card -> card instanceof Swamp);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card instanceof GravebaneZombie || card instanceof Swamp);
    }

    @Test
    void stillSearchesForASwampWhenNoZombieIsAvailable() {
        addCreatureReady(player1, new CorpseHarvester());
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLibrary(player1, List.of(new Swamp(), new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch swampSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(swampSearch).isNotNull();
        assertThat(swampSearch.params().cards()).allMatch(card -> card instanceof Swamp);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Swamp");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card instanceof Swamp);
    }

    @Test
    void canSacrificeItself() {
        addCreatureReady(player1, new CorpseHarvester());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Corpse Harvester");
        harness.assertInGraveyard(player1, "Corpse Harvester");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotActivateWhileTapped() {
        var harvester = addCreatureReady(player1, new CorpseHarvester());
        harvester.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Corpse Harvester");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        var harvester = addCreatureReady(player1, new CorpseHarvester());
        harvester.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Corpse Harvester");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillFindsAZombieWhenNoSwampIsAvailable() {
        addCreatureReady(player1, new CorpseHarvester());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLibrary(player1, List.of(new GravebaneZombie(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Gravebane Zombie");
        harness.assertInGraveyard(player1, "Corpse Harvester");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void canDeclineZombieAndStillFindSwamp() {
        addCreatureReady(player1, new CorpseHarvester());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLibrary(player1, List.of(new GravebaneZombie(), new Swamp(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Gravebane Zombie");
        harness.assertInHand(player1, "Swamp");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed(PsychogenicProbe.class)
    void shufflesOnlyOnceAfterFindingBothCards() {
        addCreatureReady(player1, new CorpseHarvester());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLibrary(player1, List.of(new GravebaneZombie(), new Swamp(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertInHand(player1, "Gravebane Zombie");
        harness.assertInHand(player1, "Swamp");
    }
}
