package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private void castResponse(UUID targetId) {
        harness.setHand(player1, List.of(new ResponseResurgence()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castModalInstant(player1, 0, 0, List.of(targetId));
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(controller.getId()).add(permanent);
        return permanent;
    }

    private Permanent addBlocker(Player controller) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, new ColossalDreadmaw());
        permanent.setBlocking(true);
        permanent.addBlockingTargetId(UUID.randomUUID());
        return permanent;
    }
}
