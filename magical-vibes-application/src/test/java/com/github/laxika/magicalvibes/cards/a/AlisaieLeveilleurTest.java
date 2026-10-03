package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlisaieLeveilleur.class, AlphinaudLeveilleur.class, GrizzlyBears.class, TurnToFrog.class})
class AlisaieLeveilleurTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Alphinaud")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card alphinaud = new AlphinaudLeveilleur();
        harness.setLibrary(player2, List.of(alphinaud));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new AlisaieLeveilleur());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(alphinaud);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the second spell each turn costs {2} less")
    void onlySecondSpellGetsCostReduction() {
        harness.addToBattlefield(player1, new AlisaieLeveilleur());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The target player can decline partner with without searching or shuffling")
    void targetPlayerCanDeclineSearch() {
        Card alphinaud = new AlphinaudLeveilleur();
        Card other = new AlisaieLeveilleur();
        harness.setLibrary(player2, List.of(alphinaud, other));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new AlisaieLeveilleur());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(alphinaud, other);
        assertThat(gameLogContains("library is shuffled")).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Partner with can target its controller and finds only one card with the partner's name")
    void controllerSearchRevealsOneMatchingCardAndShuffles() {
        Card other = new AlisaieLeveilleur();
        Card firstPartner = new AlphinaudLeveilleur();
        Card secondPartner = new AlphinaudLeveilleur();
        harness.setLibrary(player1, List.of(other, firstPartner, secondPartner));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new AlisaieLeveilleur());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstPartner);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(other, secondPartner);
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("library is shuffled")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A partner search may fail to find even with the partner in the library")
    void partnerSearchMayFailToFind() {
        Card alphinaud = new AlphinaudLeveilleur();
        harness.setLibrary(player2, List.of(alphinaud));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new AlisaieLeveilleur());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(alphinaud);
        assertThat(gameLogContains("library is shuffled")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An accepted partner search with no matching card still shuffles and finishes")
    void noMatchingPartnerStillShuffles() {
        Card other = new AlisaieLeveilleur();
        harness.setLibrary(player2, List.of(other));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new AlisaieLeveilleur());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(other);
        assertThat(gameLogContains("library is shuffled")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Casting Alisaie first lets the second spell use the full two-mana reduction")
    void alisaieCastAsFirstSpellCountsForDualcast() {
        harness.setHand(player1, List.of(new AlisaieLeveilleur(), new AlphinaudLeveilleur()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.castCreature(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertOnBattlefield(player1, "Alphinaud Leveilleur");
    }

    @Test
    @DisplayName("Dualcast cannot pay the colored part of the second spell's cost")
    void costReductionDoesNotRemoveColoredMana() {
        harness.addToBattlefield(player1, new AlisaieLeveilleur());
        harness.setHand(player1, List.of(new GrizzlyBears(), new AlisaieLeveilleur()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Dualcast does not reduce an opponent's second spell")
    void opponentsSecondSpellIsNotReduced() {
        harness.addToBattlefield(player1, new AlisaieLeveilleur());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alisaie stops reducing costs when she loses all abilities")
    void losingAllAbilitiesDisablesDualcast() {
        harness.setHand(player1, List.of(new AlisaieLeveilleur(), new AlphinaudLeveilleur()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Alisaie Leveilleur"));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Alphinaud Leveilleur");
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal lethal damage to Alisaie")
    void firstStrikeKillsBlockerBeforeNormalCombatDamage() {
        addCreatureReady(player1, new AlisaieLeveilleur());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Alisaie Leveilleur");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }
}
