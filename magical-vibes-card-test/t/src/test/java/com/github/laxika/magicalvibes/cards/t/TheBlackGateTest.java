package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBlackGate.class, GrizzlyBears.class})
class TheBlackGateTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 3 life lets The Black Gate enter untapped")
    void payingLifeEntersUntapped() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TheBlackGate()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(findPermanent(player1, "The Black Gate").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes The Black Gate enter tapped without losing life")
    void decliningLifeEntersTapped() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TheBlackGate()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findPermanent(player1, "The Black Gate").isTapped()).isTrue();
    }

    @Test
    @DisplayName("With fewer than 3 life, The Black Gate enters tapped without offering payment")
    void insufficientLifeEntersTapped() {
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new TheBlackGate()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(2);
        assertThat(findPermanent(player1, "The Black Gate").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Black Gate produces black mana")
    void producesBlackMana() {
        Permanent gate = addGateReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Chooses a player tied for most life and prevents that player's creatures from blocking")
    void choosesMostLifePlayerOnTie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addGateReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player1.getId());
        target.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("The chosen player is fixed when the ability resolves")
    void lifeChangesDoNotChangeChosenPlayer() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 20);
        addGateReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.setLife(player1, 1);
        target.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("The player with the most life is determined when the ability resolves")
    void lifeLeaderChangesBeforeResolution() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 20);
        Permanent gate = addGateReady(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.setLife(player2, 40);
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, target,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Only the targeted creature is protected, including against blockers entering later")
    void restrictionOnlyAppliesToTargetAndIncludesLaterBlockers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);
        addGateReady(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, blocker, target,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, blocker, other,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Choosing the controller does not prevent the opponent's creatures from blocking")
    void unchosenPlayerCanStillBlock() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 20);
        addGateReady(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, target,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("The restriction resolves after The Black Gate leaves and expires at end of turn")
    void restrictionSurvivesSourceLeavingButExpires() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);
        Permanent gate = addGateReady(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(gate);
        gd.playerGraveyards.get(player1.getId()).add(gate.getCard());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, target,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bls.canBlockAttacker(gd, blocker, target,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("If the target leaves before resolution, no player choice or blocking restriction occurs")
    void removedTargetDoesNotPromptForPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addGateReady(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(bls.canBlockAttacker(gd, blocker, other,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private Permanent addGateReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TheBlackGate());
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
