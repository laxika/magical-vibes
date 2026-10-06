package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfAnticipation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResponseResurgence.class, ColossalDreadmaw.class, GrizzlyBears.class})
class ResponseResurgenceTest extends BaseCardTest {

    @Test
    @DisplayName("Response deals 5 damage to an attacking creature")
    void responseDamagesAttackingCreature() {
        Permanent attacker = addAttacker(player2, player1, new ColossalDreadmaw());
        castResponse(attacker.getId());

        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Response deals 5 damage to a blocking creature")
    void responseDamagesBlockingCreature() {
        Permanent blocker = addBlocker(player2);
        castResponse(blocker.getId());

        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Response cannot target a creature that is not attacking or blocking")
    void responseRejectsNoncombatCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ResponseResurgence()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resurgence grants first strike and vigilance to your creatures and queues extra phases")
    void resurgenceGrantsKeywordsAndQueuesExtraPhases() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new ResponseResurgence()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @DisplayName("Resurgence creates an additional combat and main phase")
    void resurgenceCreatesAdditionalCombatAndMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new ResponseResurgence()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.getGameService().advanceStep(gameData);

        assertThat(gameData.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        harness.getGameService().advanceStep(gameData);
        assertThat(gameData.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        harness.getGameService().advanceStep(gameData);
        assertThat(gameData.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
        harness.getGameService().advanceStep(gameData);
        assertThat(gameData.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Response does not damage a target that has left combat before resolution")
    void responseRechecksCombatStatus() {
        Permanent attacker = addAttacker(player2, player1, new ColossalDreadmaw());
        castResponse(attacker.getId());
        attacker.setAttacking(false);

        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Response can be paid for with two white mana")
    void responseAcceptsWhiteMana() {
        Permanent attacker = addAttacker(player2, player1, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new ResponseResurgence()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castModalInstant(player1, 0, 0, List.of(attacker.getId()));

        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Resurgence cannot normally be cast during combat")
    void resurgenceRequiresSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        prepareResurgence();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({LeylineOfAnticipation.class})
    @DisplayName("Resurgence cast with flash outside a main phase grants keywords without extra phases")
    void resurgenceOutsideMainPhaseDoesNotAddPhases() {
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        prepareResurgence();

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.additionalCombatMainPhasePairs).isZero();
    }

    @Test
    @DisplayName("Resurgence affects creatures present at resolution but does not untap them or affect later arrivals")
    void resurgenceSnapshotsCreaturesAndDoesNotUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        prepareResurgence();
        harness.castModalSorcery(player1, 0, 1, List.of());
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Response has mana value two on the stack")
    void responseUsesItsOwnManaValueOnStack() {
        Permanent attacker = addAttacker(player2, player1, new ColossalDreadmaw());
        castResponse(attacker.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getManaValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resurgence has mana value five on the stack")
    void resurgenceUsesItsOwnManaValueOnStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        prepareResurgence();
        harness.castModalSorcery(player1, 0, 1, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getManaValue()).isEqualTo(5);
    }

    private void prepareResurgence() {
        harness.setHand(player1, List.of(new ResponseResurgence()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castResponse(UUID targetId) {
        harness.setHand(player1, List.of(new ResponseResurgence()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castModalInstant(player1, 0, 0, List.of(targetId));
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        return permanent;
    }

    private Permanent addBlocker(Player controller) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, new ColossalDreadmaw());
        permanent.setBlocking(true);
        permanent.addBlockingTargetId(UUID.randomUUID());
        return permanent;
    }
}
