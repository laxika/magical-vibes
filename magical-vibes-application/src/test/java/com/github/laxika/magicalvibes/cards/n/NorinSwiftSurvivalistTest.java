package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NorinSwiftSurvivalist.class, GrizzlyBears.class})
class NorinSwiftSurvivalistTest extends BaseCardTest {

    @Test
    @DisplayName("Norin cannot block")
    void cannotBlock() {
        Permanent norin = addCreatureReady(player1, new NorinSwiftSurvivalist());

        assertThat(bls.canBlock(gd, norin)).isFalse();
    }

    @Test
    @DisplayName("A blocked creature may be exiled and played this turn")
    void exilesBlockedCreatureAndAllowsItToBePlayed() {
        harness.addToBattlefield(player1, new NorinSwiftSurvivalist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        UUID attackerCardId = attacker.getOriginalCard().getId();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(attackerCardId));
        assertThat(gd.exilePlayPermissions).containsEntry(attackerCardId, player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(attackerCardId);
        assertThat(blocker.getMarkedDamage()).isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, attackerCardId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(attackerCardId));
    }

    @Test
    @DisplayName("Declining Norin's ability leaves the blocked creature in combat")
    void decliningDoesNotExileBlockedCreature() {
        harness.addToBattlefield(player1, new NorinSwiftSurvivalist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        UUID attackerCardId = attacker.getOriginalCard().getId();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getId().equals(attackerCardId));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(attackerCardId);
    }

    @Test
    @DisplayName("Norin can exile itself when blocked and be cast again")
    void canExileItselfAndBeCastAgain() {
        Permanent norin = addCreatureReady(player1, new NorinSwiftSurvivalist());
        norin.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        UUID cardId = norin.getOriginalCard().getId();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Norin, Swift Survivalist");
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(cardId));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, cardId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Norin, Swift Survivalist");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getId().equals(cardId));
    }

    @Test
    @DisplayName("Multiple blockers produce only one exile choice for a blocked creature")
    void multipleBlockersTriggerOnlyOnce() {
        harness.addToBattlefield(player1, new NorinSwiftSurvivalist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1), new BlockerAssignment(1, 1)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(attacker.getOriginalCard().getId()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Norin does not trigger for an opponent's blocked creature")
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new NorinSwiftSurvivalist());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
    @Test
    @DisplayName("Exile permission does not waive creature timing or mana costs")
    void exilePermissionRequiresNormalTimingAndMana() {
        Permanent norin = addCreatureReady(player1, new NorinSwiftSurvivalist());
        norin.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        UUID cardId = norin.getOriginalCard().getId();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, cardId))
                .isInstanceOf(IllegalStateException.class);

        gd.playerManaPools.get(player1.getId()).clear();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player1, cardId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(cardId));
        harness.assertNotOnBattlefield(player1, "Norin, Swift Survivalist");
    }
}