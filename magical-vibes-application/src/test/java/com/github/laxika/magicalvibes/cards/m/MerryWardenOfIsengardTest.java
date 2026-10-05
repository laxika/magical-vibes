package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PippinWardenOfIsengard;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerryWardenOfIsengard.class, PippinWardenOfIsengard.class, SolRing.class})
class MerryWardenOfIsengardTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Pippin")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card pippin = new PippinWardenOfIsengard();
        harness.setLibrary(player2, List.of(pippin));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new MerryWardenOfIsengard());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(pippin);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An artifact entering creates a lifelink Soldier")
    void artifactEntryCreatesLifelinkSoldier() {
        harness.addToBattlefield(player1, new MerryWardenOfIsengard());
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().getKeywords()).contains(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("The artifact trigger fires only once each turn")
    void artifactTriggerFiresOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new MerryWardenOfIsengard());
        castArtifactAndResolveTrigger();
        castArtifactAndResolveTrigger();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not trigger")
    void opponentArtifactEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new MerryWardenOfIsengard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new SolRing(), "{1}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private void castArtifactAndResolveTrigger() {
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The targeted player may decline the partner search")
    void targetPlayerMayDeclinePartnerSearch() {
        Card pippin = new PippinWardenOfIsengard();
        harness.setLibrary(player2, List.of(pippin));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new MerryWardenOfIsengard());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(pippin);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Partner search can target the controller and find no matching card")
    void controllerMaySearchWithoutFindingPippin() {
        Card otherCard = new SolRing();
        harness.setLibrary(player1, List.of(otherCard));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new MerryWardenOfIsengard());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Artifact tokens trigger Merry again on the next player's turn")
    void artifactTokenTriggersOnOpponentsTurnAfterReset() {
        harness.addToBattlefield(player1, new MerryWardenOfIsengard());
        Permanent pippin = addCreatureReady(player1, new PippinWardenOfIsengard());
        castArtifactAndResolveTrigger();
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pippin),
                0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }

    @Test
    @DisplayName("A second artifact cannot trigger Merry while the first trigger is pending")
    void secondArtifactDoesNotTriggerBeforeFirstTriggerResolves() {
        harness.addToBattlefield(player1, new MerryWardenOfIsengard());

        harness.enterBattlefieldAndReturn(player1, new SolRing());
        harness.enterBattlefieldAndReturn(player1, new SolRing());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(2);
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().isToken()).isTrue();
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(soldier.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(soldier.getCard().getKeywords()).contains(Keyword.LIFELINK);
    }
}
