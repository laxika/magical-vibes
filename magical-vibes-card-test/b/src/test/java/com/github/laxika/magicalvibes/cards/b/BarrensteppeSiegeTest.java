package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SaltRoadPackbeast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarrensteppeSiege.class, SaltRoadPackbeast.class})
class BarrensteppeSiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Abzan puts a +1/+1 counter on each creature you control at end step")
    void abzanModePutsCountersOnOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SaltRoadPackbeast());
        Permanent ownOtherCreature = harness.addToBattlefieldAndReturn(player1, new SaltRoadPackbeast());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SaltRoadPackbeast());
        Permanent siege = castAndChoose("Abzan");

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(siege.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownOtherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mardu makes each opponent sacrifice a creature after your creature dies")
    void marduModeMakesOpponentSacrifice() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SaltRoadPackbeast());
        castAndChoose("Mardu");
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCreature.getCard());
    }

    @Test
    @DisplayName("Mardu does nothing when no creature died under your control")
    void marduModeDoesNothingWithoutOwnCreatureDeath() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SaltRoadPackbeast());
        castAndChoose("Mardu");

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    void abzanDoesNotTriggerOnOpponentsEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SaltRoadPackbeast());
        castAndChoose("Abzan");

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void abzanTriggerResolvesAfterSiegeLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SaltRoadPackbeast());
        Permanent siege = castAndChoose("Abzan");
        advanceToEndStep(player1);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, siege);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void abzanIncludesCreaturesAddedBeforeResolution() {
        castAndChoose("Abzan");
        advanceToEndStep(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SaltRoadPackbeast());

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void marduIgnoresDeathsUnderOpponentsControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SaltRoadPackbeast());
        castAndChoose("Mardu");
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void marduOpponentChoosesOneCreatureEvenAfterMultipleDeaths() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new SaltRoadPackbeast());
        Permanent kept = harness.addToBattlefieldAndReturn(player2, new SaltRoadPackbeast());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SaltRoadPackbeast());
        castAndChoose("Mardu");
        gd.creatureDeathCountThisTurn.put(player1.getId(), 2);
        advanceToEndStep(player1);

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(chosen.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kept).doesNotContain(chosen);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void marduTriggerResolvesAfterSiegeLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SaltRoadPackbeast());
        Permanent siege = castAndChoose("Mardu");
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        advanceToEndStep(player1);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, siege);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }
    private Permanent castAndChoose(String mode) {
        harness.setHand(player1, List.of(new BarrensteppeSiege()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("Abzan", "Mardu");
        harness.handleListChoice(player1, mode);

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BarrensteppeSiege)
                .findFirst()
                .orElseThrow();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
