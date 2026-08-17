package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThiefOfSanityTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles one of the damaged player's top three cards and puts the rest into their graveyard")
    void combatDamageExilesOneTopCardAndMillsTheRest() {
        Card topCard = new Forest();
        Card chosenCard = new Island();
        Card thirdCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, chosenCard, thirdCard));
        Permanent thief = addAttackingThief();

        resolveCombatAndTrigger();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice).isNotNull();
        assertThat(choice.params().playerId()).isEqualTo(player1.getId());
        assertThat(choice.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.params().cards()).containsExactly(topCard, chosenCard, thirdCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.getCardsExiledByPermanent(thief.getId())).containsExactly(chosenCard);
        assertThat(gd.exiledCards).filteredOn(entry -> entry.card().getId().equals(chosenCard.getId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard, thirdCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The exiled card remains castable with any mana after Thief of Sanity leaves")
    void exiledCardCanBeCastAfterThiefLeaves() {
        Card stolenCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(stolenCard, new Forest(), new Island()));
        Permanent thief = addAttackingThief();
        resolveCombatAndTrigger();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, thief.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, stolenCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A blocked Thief of Sanity does not trigger")
    void blockedDoesNotTrigger() {
        addAttackingThief();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player2, List.of(new Forest(), new Island(), new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addAttackingThief() {
        Permanent thief = harness.addToBattlefieldAndReturn(player1, new ThiefOfSanity());
        thief.setSummoningSick(false);
        thief.setAttacking(true);
        return thief;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
