package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProfessionalFaceBreaker.class, GrizzlyBears.class, Forest.class})
class ProfessionalFaceBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Menace requires two blockers and combat damage to creatures creates no Treasure")
    void menaceRequiresTwoBlockersAndBlockedDamageCreatesNoTreasure() {
        addCreatureReady(player1, new ProfessionalFaceBreaker());
        Permanent firstBlocker = addCreatureReady(player2, new ProfessionalFaceBreaker());
        addCreatureReady(player2, new ProfessionalFaceBreaker());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(firstBlocker.getId(), 2));
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Professional Face-Breaker");
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Unplayed cards remain exiled after play permission expires")
    void playPermissionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new ProfessionalFaceBreaker());
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Professional Face-Breaker triggers for its own combat damage")
    void createsTreasureForItsOwnCombatDamage() {
        addCreatureReady(player1, new ProfessionalFaceBreaker());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Professional Face-Breaker triggers independently")
    void eachCopyCreatesATreasure() {
        addCreatureReady(player1, new ProfessionalFaceBreaker());
        addCreatureReady(player1, new ProfessionalFaceBreaker());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent combat damage does not trigger Professional Face-Breaker")
    void opponentCombatDamageDoesNotCreateTreasure() {
        harness.addToBattlefield(player1, new ProfessionalFaceBreaker());
        addCreatureReady(player2, new ProfessionalFaceBreaker());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the exile ability without a Treasure")
    void requiresTreasureToActivate() {
        harness.addToBattlefield(player1, new ProfessionalFaceBreaker());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("Treasure is sacrificed as a cost before the top card is exiled")
    void sacrificeIsPaidBeforeResolutionAndLandCanBePlayed() {
        addCreatureReady(player1, new ProfessionalFaceBreaker());
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();

        harness.passBothPriorities();
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("An exiled creature requires its normal mana cost")
    void exiledCreatureRequiresManaPayment() {
        addCreatureReady(player1, new ProfessionalFaceBreaker());
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        Card topCard = new ProfessionalFaceBreaker();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Professional Face-Breaker")).isEqualTo(2);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("An empty library still requires sacrificing the Treasure")
    void emptyLibraryStillConsumesTreasure() {
        addCreatureReady(player1, new ProfessionalFaceBreaker());
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.setLibrary(player1, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Creates only one Treasure when multiple creatures deal combat damage to a player")
    void createsOneTreasureForMultipleCombatDamageDealers() {
        addCreatureReady(player1, new ProfessionalFaceBreaker());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a Treasure exiles the top card with permission to play it this turn")
    void sacrificeTreasureExilesTopCardWithPlayPermission() {
        addCreatureReady(player1, new ProfessionalFaceBreaker());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
    }
}
