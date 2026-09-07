package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalExecutioner.class, GrizzlyBears.class})
class CabalExecutionerTest extends BaseCardTest {

    @Test
    void damagedPlayerChoosesACreatureToSacrifice() {
        Permanent executioner = addCreatureReady(player1, new CabalExecutioner());
        executioner.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondEnemyCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(enemyCreature.getId(), secondEnemyCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player2, enemyCreature.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    void blockedExecutionerDoesNotTrigger() {
        Permanent executioner = addCreatureReady(player1, new CabalExecutioner());
        executioner.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noTriggerWhenDamagedPlayerControlsNoCreatures() {
        Permanent executioner = addCreatureReady(player1, new CabalExecutioner());
        executioner.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new CabalExecutioner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent executioner = findPermanent(player1, "Cabal Executioner");
        assertThat(executioner.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(executioner));
        harness.passBothPriorities();

        assertThat(executioner.isFaceDown()).isFalse();
    }
}
