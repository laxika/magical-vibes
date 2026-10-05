package com.github.laxika.magicalvibes.cards.l;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({LadyLokiAgentOfChaos.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class,
        Cancel.class, Blaze.class})
class LadyLokiAgentOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("The first matching spell is exiled, deals the mana-value difference, and offers a free cast")
    void firstMatchingSpellExilesAndDealsDifference() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        Forest forest = new Forest();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, hit));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(forest, hit,
                gd.getPlayerExiledCards(player1.getId()).stream()
                        .filter(card -> card.getName().equals("Counsel of the Soratami"))
                        .findFirst().orElseThrow());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).anyMatch(stackEntry -> stackEntry.getPhysicalCard() == hit);
    }

    @Test
    @DisplayName("Only the first instant, sorcery, or Villain spell each turn triggers")
    void onlyFirstMatchingSpellTriggers() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CounselOfTheSoratami(), new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.castSorcery(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void counteredTriggeringSpellStillDigsDealsDamageAndOffersCast() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(hit));
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(hit).doesNotContain(spell);
        harness.assertLife(player2, 19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).anyMatch(entry -> entry.getPhysicalCard() == hit);
    }

    @Test
    void villainSpellTriggersAndDecliningLeavesHitExiled() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        LadyLokiAgentOfChaos spell = new LadyLokiAgentOfChaos();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(hit));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell, hit);
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(hit);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void higherManaValueHitUsesAbsoluteDifference() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.setLibrary(player1, List.of(new LadyLokiAgentOfChaos()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void equalManaValuesDealNoDamageAndStillOfferCast() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        CounselOfTheSoratami hit = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.setLibrary(player1, List.of(hit));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).hasSize(1).anyMatch(entry -> entry.getPhysicalCard() == hit);
    }

    @Test
    void allLandLibraryIsExiledWithoutDamageOrCastOffer() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        Forest first = new Forest();
        Forest second = new Forest();
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell, first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void nonmatchingCreatureDoesNotConsumeFirstMatchingSpell() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        harness.setHand(player1, List.of(new GrizzlyBears(), new CounselOfTheSoratami()));
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(hit));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(hit);
        harness.assertLife(player2, 19);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void chosenXContributesToTriggeringSpellManaValue() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        Blaze spell = new Blaze();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void exiledXCardHasZeroXForDamage() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        Blaze hit = new Blaze();
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.setLibrary(player1, List.of(hit));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(hit);
    }

    @Test
    void instantOnOpponentsTurnTriggersWithoutExilingOpponentsSpell() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        GrizzlyBears opposingSpell = new GrizzlyBears();
        GrizzlyBears hit = new GrizzlyBears();
        Cancel spell = new Cancel();
        harness.setHand(player2, List.of(opposingSpell));
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(hit));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, opposingSpell.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell, hit);
        assertThat(gd.stack).anyMatch(entry -> entry.getPhysicalCard() == opposingSpell);
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        harness.handleMayAbilityChosen(player1, false);
    }
}
