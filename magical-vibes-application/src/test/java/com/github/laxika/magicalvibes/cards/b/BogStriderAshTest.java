package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Facevaulter;
import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogStriderAsh.class, Facevaulter.class, LeafGilder.class, WoodlandChangeling.class, Swamp.class})
class BogStriderAshTest extends BaseCardTest {

    private void giveGoblinSpell(com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new Facevaulter()));
        harness.addMana(caster, ManaColor.BLACK, 5);
    }

    @Test
    @DisplayName("Casting a Goblin spell prompts the controller's may-pay ability")
    void goblinSpellTriggersMayPay() {
        harness.addToBattlefield(player1, new BogStriderAsh());
        giveGoblinSpell(player1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting and paying {G} gains 2 life")
    void acceptAndPayGainsLife() {
        harness.addToBattlefield(player1, new BogStriderAsh());

        // Opponent casts the Goblin so player1's green mana is reserved for the {G} payment.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        giveGoblinSpell(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining leaves life unchanged")
    void declineLeavesLifeUnchanged() {
        harness.addToBattlefield(player1, new BogStriderAsh());
        giveGoblinSpell(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Accepting without enough mana gains no life")
    void acceptWithoutManaNoLife() {
        harness.addToBattlefield(player1, new BogStriderAsh());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        giveGoblinSpell(player2);
        // No green mana for player1.

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Casting a non-Goblin spell does not trigger the ability")
    void nonGoblinDoesNotTrigger() {
        harness.addToBattlefield(player1, new BogStriderAsh());
        harness.setHand(player1, List.of(new LeafGilder()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Any player casting a Goblin spell triggers the controller's ability")
    void opponentGoblinTriggersController() {
        harness.addToBattlefield(player1, new BogStriderAsh());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        giveGoblinSpell(player2);

        harness.castCreature(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The optional payment is chosen only when the triggered ability resolves")
    void paymentWaitsForResolution() {
        harness.addToBattlefield(player1, new BogStriderAsh());
        giveGoblinSpell(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.castCreature(player1, 0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A changeling spell is a Goblin spell and allows the life-gain payment")
    void changelingSpellTriggersLifeGain() {
        harness.addToBattlefield(player1, new BogStriderAsh());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WoodlandChangeling()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Swampwalk prevents blocking when the defending player controls a Swamp")
    void defendingSwampPreventsBlocking() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new LeafGilder());
        Permanent attacker = addCreatureReady(player1, new BogStriderAsh());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("A Swamp controlled only by the attacker does not prevent blocking")
    void attackingSwampDoesNotPreventBlocking() {
        Permanent attacker = addCreatureReady(player1, new BogStriderAsh());
        harness.addToBattlefield(player1, new Swamp());
        Permanent blocker = addCreatureReady(player2, new LeafGilder());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
